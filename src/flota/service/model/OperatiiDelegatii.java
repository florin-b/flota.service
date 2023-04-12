package flota.service.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.maps.model.LatLng;

import flota.service.beans.AdresaOprire;
import flota.service.beans.BeanDelegatieAprobare;
import flota.service.beans.BeanDelegatieCauta;
import flota.service.beans.BeanDelegatieGenerata;
import flota.service.beans.DelegatieModifAntet;
import flota.service.beans.DelegatieModifDetalii;
import flota.service.beans.DelegatieNoua;
import flota.service.beans.PunctTraseu;
import flota.service.beans.PunctTraseuLite;
import flota.service.beans.Traseu;
import flota.service.database.DBManager;
import flota.service.helpers.HelperAprobare;
import flota.service.helpers.HelperDelegatie;
import flota.service.queries.SqlQueries;
import flota.service.utils.DateUtils;
import flota.service.utils.MailOperations;
import flota.service.utils.MapUtils;
import flota.service.utils.Utils;
import flota.service.utils.UtilsAddress;

public class OperatiiDelegatii {

	private static final Logger logger = LogManager.getLogger(OperatiiDelegatii.class);

	public synchronized boolean adaugaDelegatie(DelegatieNoua delegatie) {

		System.out.println("adaugaDelegatie " + delegatie);

		boolean success = true;

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.adaugaAntetDelegatie())) {

			List<String> listAuto = new OperatiiMasina().getMasiniAngajat(conn, delegatie.getCodAngajat());

			if (listAuto.isEmpty() || !listAuto.contains(delegatie.getNrAuto().replace("-", "").replace(" ", ""))) {
				return false;
			}

			String idDelegatieNoua = Utils.getId();

			stmt.clearParameters();
			stmt.setString(1, idDelegatieNoua);
			stmt.setString(2, delegatie.getCodAngajat());
			stmt.setString(3, DateUtils.getCurrentDate());
			stmt.setString(4, DateUtils.getCurrentTime());
			stmt.setString(5, DateUtils.formatDateSap(delegatie.getDataP()));
			stmt.setString(6, delegatie.getOraP());
			stmt.setDouble(7, (int) Double.parseDouble(delegatie.getDistcalc()));
			stmt.setString(8, HelperAprobare.getCodAprobare(conn, delegatie));
			stmt.setString(9, DateUtils.formatDateSap(delegatie.getDataS()));
			stmt.setString(10, delegatie.getNrAuto());
			stmt.setDouble(11, (int) Double.parseDouble(delegatie.getDistreal()));

			stmt.executeQuery();

			String[] arrayOpriri = delegatie.getStops().split(",");

			int ord = 0;

			String punctVizitat = "0";

			if (Double.parseDouble(delegatie.getDistreal()) > 0)
				punctVizitat = "1";

			try (PreparedStatement stmt1 = conn.prepareStatement(SqlQueries.adaugaOpririDelegatie())) {

				for (int i = 0; i < arrayOpriri.length; i++) {

					String[] arrayAdresa = arrayOpriri[i].trim().split("/");

					stmt1.clearParameters();
					stmt1.setString(1, idDelegatieNoua);

					ord++;

					stmt1.setString(2, String.valueOf(ord));
					stmt1.setString(3, arrayAdresa[1].trim());
					stmt1.setString(4, arrayAdresa[0].trim());
					stmt1.setString(5, punctVizitat);
					stmt1.setString(6, "1");

					stmt1.executeQuery();

				}
			}
			
			if (delegatie.getTipAngajat().equals("DZ") || delegatie.getTipAngajat().equals("DD") || delegatie.getTipAngajat().equals("DDCVA"))
		 		aprobaDelegatie(idDelegatieNoua, delegatie.getTipAngajat(), "0", delegatie.getCodAngajat(), "2");

		} catch (SQLException e) {
			logger.error("Opriri: " + delegatie.getStops() + Utils.getStackTrace(e));
			MailOperations.sendMail(Utils.getStackTrace(e));
			success = false;
		}

		return success;

	}

	public List<BeanDelegatieAprobare> getDelegatiiAprobari(String tipAngajat, String unitLog, String codDepart) {

		System.out.println("getDelegatiiAprobari: " +  tipAngajat  + " , " +  unitLog + " , " + codDepart);
		
		if (tipAngajat.equals("SDIP"))
			codDepart = "11";
		
		boolean isPersVanzari = Utils.isAngajatVanzari(tipAngajat);

		verificaDelegatiiTerminate(tipAngajat, unitLog, codDepart, isPersVanzari);

		List<BeanDelegatieAprobare> listDelegatii = new ArrayList<>();
		
		if (tipAngajat.equals("DD") && codDepart.startsWith("04"))
			unitLog += ",GL90"; 

		String unitLogQs = Utils.generateQs(unitLog);

		String departQs = Utils.generateQs(codDepart);

		String sqlString;

		if (isPersVanzari)
			sqlString = SqlQueries.getDelegatiiAprobareHeaderVanzari(unitLogQs, departQs);
		else {
			if (tipAngajat.equals("DZ"))
				sqlString = SqlQueries.getDelegatiiAprobareHeaderNONVanzari_DZ(unitLogQs);
			else if (tipAngajat.equals("DAG") || tipAngajat.equals("DLOG"))
				sqlString = SqlQueries.getDelegatiiAprobareHeaderNONVanzari_DAG(unitLogQs);
			else
				sqlString = SqlQueries.getDelegatiiAprobareHeaderNONVanzari(unitLogQs);
		}
		

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			
			stmt.setString(1, tipAngajat);

			int pos = 2;

			String[] unitLogs = unitLog.split(",");

			String[] departs = codDepart.split(",");
			

			for (int ii = 0; ii < unitLogs.length; ii++)
				stmt.setString(pos++, unitLogs[ii]);

			if (isPersVanzari) {

				for (int ii = 0; ii < departs.length; ii++)
					stmt.setString(pos++, departs[ii].substring(0, 2));
			} else {

				if (!tipAngajat.equals("DZ"))
					stmt.setString(pos++, tipAngajat);
			}
			

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			OperatiiAngajat opAngajat = new OperatiiAngajat();

			while (rs.next()) {

				double distReal = rs.getDouble("distreal");
				String statusDel = HelperDelegatie.getStatusDelegatie(conn, rs.getString("id"));

				if (distReal == -1)
					continue;

				if (statusDel.equals("6"))
					continue;

				if (distReal == 0) {
					if (statusDel.equals("1"))
						continue;
				} else {
					if (statusDel.equals("2"))
						continue;
				}

				BeanDelegatieAprobare delegatie = new BeanDelegatieAprobare();
				delegatie.setId(rs.getString("id"));
				delegatie.setDataPlecare(DateUtils.formatDateFromSap(rs.getString("data_plecare")));
				delegatie.setDataSosire(DateUtils.formatDateFromSap(rs.getString("data_sosire")));
				delegatie.setOraPlecare(DateUtils.formatTime(rs.getString("ora_plecare")));
				delegatie.setNumeAngajat(rs.getString("nume"));
				delegatie.setListOpriri(getOpriri(conn, delegatie.getId()));

				int kmCota = opAngajat.getKmCota(conn, rs.getString("codAngajat"), delegatie.getDataPlecare(), delegatie.getDataSosire());

				int kmCalc = (int) rs.getDouble("distcalc");
				int kmRecalc = (int) rs.getDouble("distrecalc");

				int distCalc = kmCalc + kmCota;

				int distRecalc = 0;
				if (kmRecalc > 0)
					distRecalc = kmRecalc + kmCota;

				if (distRecalc == -1)
					distRecalc = 0;

				if (kmCalc == 0)
					distCalc = 0;

				if (kmRecalc == 0)
					distRecalc = 0;

				delegatie.setDistantaCalculata(distCalc);
				delegatie.setDistantaRecalculata(distRecalc);

				delegatie.setDistantaRespinsa((int) rs.getDouble("distrespins"));
				delegatie.setDistantaEfectuata((int) rs.getDouble("distreal"));

				delegatie.setMsgAtentionare(HelperDelegatie.getAtentionare(delegatie));
				delegatie.setWeekend(rs.getString("codAprob").equals("WKND"));
				listDelegatii.add(delegatie);
			}

			rs.close();

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(Utils.getStackTrace(e));
		}

		return listDelegatii;

	}

	public void aprobaDelegatie(String idDelegatie, String tipAngajat, String kmRespinsi, String codAngajat, String tipAprobare) {
		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.opereazaDelegatie(), ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);) {

			int dKmRespinsi = Integer.parseInt(kmRespinsi);

			stmt.setString(1, idDelegatie);
			stmt.setString(2, tipAngajat);
			stmt.setString(3, getCodAprobare(tipAprobare));
			stmt.setString(4, DateUtils.getCurrentDate());
			stmt.setString(5, DateUtils.getCurrentTime());
			stmt.setString(6, codAngajat);

			stmt.executeQuery();

			if (dKmRespinsi > 0) {

				try (PreparedStatement stmt1 = conn.prepareStatement(SqlQueries.setKmRespinsi());) {
					stmt1.setDouble(1, (int) dKmRespinsi);
					stmt1.setString(2, idDelegatie);

					stmt1.executeQuery();

				}

			}

		} catch (Exception ex) {
			logger.error(Utils.getStackTrace(ex) + " id: " + idDelegatie);
			MailOperations.sendMail(Utils.getStackTrace(ex) + " id: " + idDelegatie + " , tipAng = " + tipAngajat + " , codAprob=" + getCodAprobare(tipAprobare)
					+ " , conAng =" + codAngajat);
		}

	}

	public void aprobaAutomatDelegatie(Connection conn, String idDelegatie) {
		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.aprobaAutomatDelegatie());) {

			stmt.setString(1, idDelegatie);
			stmt.setString(2, DateUtils.getCurrentDate());
			stmt.setString(3, DateUtils.getCurrentTime());

			stmt.executeQuery();

		} catch (Exception ex) {
			logger.error(Utils.getStackTrace(ex));
			MailOperations.sendMail(ex.toString());
		}

	}

	public void respingeDelegatie(String idDelegatie, String tipAngajat, String codAngajat) {

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.opereazaDelegatie(), ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, idDelegatie);
			stmt.setString(2, tipAngajat);
			stmt.setString(3, "6");
			stmt.setString(4, DateUtils.getCurrentDate());
			stmt.setString(5, DateUtils.getCurrentTime());
			stmt.setString(6, codAngajat);

			stmt.executeQuery();

		} catch (Exception ex) {
			MailOperations.sendMail(ex.toString());
			logger.error(Utils.getStackTrace(ex));
		}

	}

	public List<String> getLocOpriri(Connection conn, String idDelegatie) {
		List<String> listLocalitati = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDelegatiiAprobareRuta());) {

			stmt.setString(1, idDelegatie);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				listLocalitati.add(rs.getString("judet") + " / " + rs.getString("localitate"));
			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return listLocalitati;
	}

	public List<PunctTraseu> getPuncteTraseu(Connection conn, String idDelegatie) {
		List<PunctTraseu> listLocalitati = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getPuncteTraseu());) {

			stmt.setString(1, idDelegatie);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				PunctTraseu punct = new PunctTraseu();
				punct.setStrAdresa(rs.getString("judet") + " / " + rs.getString("localitate"));
				punct.setPozitie(Integer.valueOf(rs.getString("poz")));

				if (!rs.getString("lat").equals("-1")) {
					punct.setCoordonate(new LatLng(Double.valueOf(rs.getString("lat")), Double.valueOf(rs.getString("lon"))));
				} else {

					LatLng coordPunct = MapUtils.geocodeAddress(UtilsAddress.getAddress(punct.getStrAdresa()));
					punct.setCoordonate(new LatLng(coordPunct.lat, coordPunct.lng));

					//if (coordPunct.lat > 0 && !existaCoordonatePunct(conn, punct.getStrAdresa()))
				   //	salveazaCoordonatePunct(conn, punct.getStrAdresa(), coordPunct);

				}

				listLocalitati.add(punct);

			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return listLocalitati;
	}

	private boolean existaCoordonatePunct(Connection conn, String adresa) {

		boolean exista = false;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.existaCoordonate());) {

			stmt.setString(1, adresa.split("/")[0]);
			stmt.setString(2, adresa.split("/")[1]);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				exista = true;
			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return exista;

	}

	private void salveazaCoordonatePunct(Connection conn, String adresa, LatLng coordonate) {

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.adaugaCoordonate());) {

			stmt.setString(1, adresa.split("/")[0]);
			stmt.setString(2, adresa.split("/")[1]);
			stmt.setString(3, String.valueOf(coordonate.lat));
			stmt.setString(4, String.valueOf(coordonate.lng));

			stmt.executeQuery();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

	}

	public List<PunctTraseuLite> getOpriri(Connection conn, String idDelegatie) {

		List<PunctTraseuLite> listOpriri = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDelegatiiAprobareRuta());) {

			stmt.setString(1, idDelegatie);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				PunctTraseuLite punct = new PunctTraseuLite();
				punct.setAdresa(rs.getString("judet") + " / " + rs.getString("localitate"));
				punct.setVizitat(rs.getString("vizitat").equals("1") ? true : false);
				punct.setInit(rs.getString("init").equals("1") ? true : false);

				listOpriri.add(punct);

			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return listOpriri;
	}

	public List<BeanDelegatieAprobare> afiseazaDelegatiiProprii(String codAngajat, String dataStart, String dataStop) {

		List<BeanDelegatieAprobare> listDelegatii = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.afiseazaDelegatiiProprii(), ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));
			stmt.setString(3, DateUtils.formatDateSap(dataStop));
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			OperatiiAngajat opAngajat = new OperatiiAngajat();

			while (rs.next()) {
				BeanDelegatieAprobare delegatie = new BeanDelegatieAprobare();
				delegatie.setId(rs.getString("id"));
				delegatie.setDataPlecare(DateUtils.formatDateFromSap(rs.getString("data_plecare")));
				delegatie.setDataSosire(DateUtils.formatDateFromSap(rs.getString("data_sosire")));
				delegatie.setOraPlecare(DateUtils.formatTime(rs.getString("ora_plecare")));
				delegatie.setNumeAngajat(" ");
				delegatie.setListOpriri(getOpriri(conn, delegatie.getId()));

				int kmCota = opAngajat.getKmCota(conn, rs.getString("codAngajat"), delegatie.getDataPlecare(), delegatie.getDataSosire());

				delegatie.setDistantaCalculata((int) rs.getDouble("distcalc") + kmCota);

				delegatie.setDistantaRespinsa((int) rs.getDouble("distrespins"));
				delegatie.setDistantaEfectuata((int) rs.getDouble("distreal"));
				delegatie.setStatusCode(HelperDelegatie.getStatusDelegatie(conn, delegatie.getId()));

				int distRecalc = 0;
				if (rs.getDouble("distrecalc") > 0)
					distRecalc = (int) rs.getDouble("distrecalc") + kmCota;

				delegatie.setDistantaRecalculata((int) rs.getDouble("distrecalc") + kmCota);
				delegatie.setDistantaRecalculata(distRecalc);

				listDelegatii.add(delegatie);
			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return listDelegatii;

	}

	public List<BeanDelegatieAprobare> afiseazaDelegatiiSubord(String dataStart, String dataStop, String tipAngajat, String unitLog, String codDepart) {

		List<BeanDelegatieAprobare> listDelegatii = new ArrayList<>();

		boolean isPersVanzari = Utils.isAngajatVanzari(tipAngajat);

		String sqlString = "";

		String unitLogQs = Utils.generateQs(unitLog);

		String departQs = Utils.generateQs(codDepart);

		if (isPersVanzari)
			sqlString = SqlQueries.afiseazaDelegatiiSubordVanzari(unitLogQs, departQs);
		else {
			if (tipAngajat.equals("DJ"))
				sqlString = SqlQueries.afiseazaDelegatiiSubordDirectorJuridic();
			else if (tipAngajat.equals("DLOG"))
				sqlString = SqlQueries.afiseazaDelegatiiSubordDirectorLogistica();
			else
				sqlString = SqlQueries.afiseazaDelegatiiSubordNONVanzari(unitLogQs);
		}

		System.out.println("afiseazaDelegatiisubord: " + sqlString + " , " + tipAngajat + " , " + unitLog + " , " + codDepart);

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			int pos = 2;
			String[] unitLogs = unitLog.split(",");
			String[] departs = codDepart.split(",");

			if (isPersVanzari) {
				stmt.setString(1, tipAngajat);

				for (int ii = 0; ii < unitLogs.length; ii++)
					stmt.setString(pos++, unitLogs[ii]);

				for (int ii = 0; ii < departs.length; ii++)
					stmt.setString(pos++, departs[ii].substring(0, 2));

				stmt.setString(pos++, DateUtils.formatDateSap(dataStart));
				stmt.setString(pos++, DateUtils.formatDateSap(dataStop));
			} else {

				if (tipAngajat.equals("DJ") || tipAngajat.equals("DLOG")) {
					stmt.setString(1, DateUtils.formatDateSap(dataStart));
					stmt.setString(2, DateUtils.formatDateSap(dataStop));
				} else {
					stmt.setString(1, tipAngajat);

					for (int ii = 0; ii < unitLogs.length; ii++)
						stmt.setString(pos++, unitLogs[ii]);

					stmt.setString(pos++, DateUtils.formatDateSap(dataStart));
					stmt.setString(pos++, DateUtils.formatDateSap(dataStop));
				}
			}

			stmt.executeQuery();

			OperatiiAngajat opAngajat = new OperatiiAngajat();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				BeanDelegatieAprobare delegatie = new BeanDelegatieAprobare();
				delegatie.setId(rs.getString("id"));
				delegatie.setDataPlecare(DateUtils.formatDateFromSap(rs.getString("data_plecare")));
				delegatie.setDataSosire(DateUtils.formatDateFromSap(rs.getString("data_sosire")));
				delegatie.setOraPlecare(DateUtils.formatTime(rs.getString("ora_plecare")));
				delegatie.setNumeAngajat(rs.getString("nume"));
				delegatie.setListOpriri(getOpriri(conn, delegatie.getId()));

				int kmCota = opAngajat.getKmCota(conn, rs.getString("codAngajat"), delegatie.getDataPlecare(), delegatie.getDataSosire());

				delegatie.setDistantaCalculata((int) rs.getDouble("distcalc") + kmCota);

				delegatie.setDistantaRespinsa((int) rs.getDouble("distrespins"));
				delegatie.setDistantaEfectuata((int) rs.getDouble("distreal"));
				delegatie.setStatusCode(HelperDelegatie.getStatusDelegatie(conn, delegatie.getId()));
				delegatie.setDistantaRecalculata((int) rs.getDouble("distrecalc"));

				listDelegatii.add(delegatie);
			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return listDelegatii;

	}

	public BeanDelegatieCauta getDelegatie(Connection conn, String idDelegatie) {

		BeanDelegatieCauta delegatie = new BeanDelegatieCauta();

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDelegatieCauta(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, idDelegatie);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				delegatie.setDataPlecare(DateUtils.formatDateFromSap(rs.getString("data_plecare")));
				delegatie.setOraPlecare(rs.getString("ora_plecare"));
				delegatie.setDataSosire(DateUtils.formatDateFromSap(rs.getString("data_sosire")));
				delegatie.setDistantaCalculata((int) rs.getDouble("distcalc"));
				delegatie.setAngajatId(rs.getString("codangajat"));
			}

			rs.close();

		}

		catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return delegatie;

	}

	private static String getCodAprobare(String codAprobare) {
		if (codAprobare.equals("0"))
			return "1";

		return "2";
	}

	public void verificaDelegatiiTerminate(String tipAngajat, String unitLog, String depart, boolean isPersVanzari) {

		String sqlString;

		String unitLogQs = Utils.generateQs(unitLog);

		if (tipAngajat.equals("DAG") || tipAngajat.equals("DLOG"))
			sqlString = SqlQueries.getDelegatiiTerminateNONVanzari_DAG(unitLogQs);
		else {
			if (isPersVanzari)
				sqlString = SqlQueries.getDelegatiiTerminateVanzari(unitLogQs);
			else
				sqlString = SqlQueries.getDelegatiiTerminateNONVanzari(unitLogQs);
		}

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			int pos = 2;
			String[] unitLogs = unitLog.split(",");

			if (isPersVanzari) {
				stmt.setString(1, tipAngajat);

				for (int ii = 0; ii < unitLogs.length; ii++)
					stmt.setString(pos++, unitLogs[ii]);

				stmt.setString(pos++, depart);
			} else {
				stmt.setString(1, tipAngajat);

				for (int ii = 0; ii < unitLogs.length; ii++)
					stmt.setString(pos++, unitLogs[ii]);

			}

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			OperatiiTraseu opTraseu = new OperatiiTraseu();

			while (rs.next()) {
				opTraseu.determinaSfarsitDelegatie(conn, rs.getString("id"));

			}

			rs.close();

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(Utils.getStackTrace(e));
		}

		return;

	}

	public void verificaDelegatiiTerminateCompanie() {

		String sqlString;

		sqlString = SqlQueries.getDelegatiiTerminateCompanie();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			OperatiiTraseu opTraseu = new OperatiiTraseu();

			int nrDel = 0;

			while (rs.next()) {
				opTraseu.determinaSfarsitDelegatie(conn, rs.getString("id"));
				nrDel++;

			}

			MailOperations.sendMail("Delegatii Service", " Au fost verificate " + nrDel);

			rs.close();

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(Utils.getStackTrace(e));
		}

		return;

	}

	public List<DelegatieModifAntet> getDelegatiiModificare(String codAngajat) {

		List<DelegatieModifAntet> listDelegatii = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDelModifHeader(), ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, codAngajat);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				DelegatieModifAntet delegatie = new DelegatieModifAntet();

				delegatie.setId(rs.getString("id"));
				delegatie.setDataPlecare(DateUtils.formatDateFromSap(rs.getString("data_plecare")));
				delegatie.setDataSosire(DateUtils.formatDateFromSap(rs.getString("data_sosire")));

				String startStopD = getStartStopDelegatie(conn, delegatie.getId());

				delegatie.setLocalitateStart(startStopD.split("#")[0]);
				delegatie.setLocalitateStop(startStopD.split("#")[1]);
				listDelegatii.add(delegatie);

			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return listDelegatii;

	}

	private String getStartStopDelegatie(Connection conn, String idDelegatie) {

		String startLoc = "";
		String stopLoc = "";

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDelModifStartStop());) {

			stmt.setString(1, idDelegatie);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			int i = 0;
			while (rs.next()) {

				if (i == 0)
					startLoc = rs.getString("punct");

				stopLoc = rs.getString("punct");

				i++;

			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return startLoc + "#" + stopLoc;

	}

	public DelegatieModifDetalii getDelegatieModif(String delegatieId) {

		DelegatieModifDetalii delegatie = new DelegatieModifDetalii();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDelegatieModif(), ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, delegatieId);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				delegatie.setNrAuto(rs.getString("nrauto"));
				delegatie.setDataPlecare(DateUtils.formatDateFromSap(rs.getString("data_plecare")));
				delegatie.setOraPlecare(rs.getString("ora_plecare"));
				delegatie.setDataSosire(DateUtils.formatDateFromSap(rs.getString("data_sosire")));

				List<PunctTraseuLite> ruta = getOpriri(conn, delegatieId);

				delegatie.setLocPlecare(ruta.get(0).getAdresa());
				delegatie.setLocSosire(ruta.get(ruta.size() - 1).getAdresa());

				ruta.remove(0);
				ruta.remove(ruta.size() - 1);

				List<String> puncte = new ArrayList<>();

				for (PunctTraseuLite punct : ruta)
					puncte.add(punct.getAdresa());

				delegatie.setRuta(puncte);
			}

			rs.close();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return delegatie;

	}

	public BeanDelegatieGenerata genereazaDelegatie(String codAngajat, String dataStart, String dataStop) {

		BeanDelegatieGenerata delegatie = new BeanDelegatieGenerata();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();) {

			Traseu traseu = new OperatiiTraseu().getTraseuAngajat(conn, codAngajat, dataStart, dataStop);

			List<AdresaOprire> adreseOpriri = MapUtils.getAdreseCoordonate(traseu.getCoordonate());

			List<PunctTraseuLite> listPuncte = new ArrayList<>();

			for (AdresaOprire adresa : adreseOpriri) {
				PunctTraseuLite punct = new PunctTraseuLite();
				punct.setAdresa(adresa.getAdresa().split("/")[1] + " / " + adresa.getAdresa().split("/")[0].toUpperCase());
				punct.setInit(true);
				punct.setVizitat(true);
				listPuncte.add(punct);

			}

			int kmCota = new OperatiiAngajat().getKmCota(conn, codAngajat, dataStart, dataStop);

			int distantaTeoretica = MapUtils.getDistantaTraseuCoordonate(adreseOpriri);

			delegatie.setDataPlecare(dataStart);
			delegatie.setDataSosire(dataStop);
			delegatie.setListOpriri(listPuncte);
			delegatie.setDistantaCalculata(distantaTeoretica);
			delegatie.setDistantaEfectuata(traseu.getDistanta());
			delegatie.setNrAuto(traseu.getNrAuto());
			delegatie.setKmCota(kmCota);

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return delegatie;

	}

	public String getDelegatiiSuprapuse(String codAngajat, String dataStart, String dataStop) {

		StringBuilder idDelegatie = new StringBuilder();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.delegatiiSuprapuse())) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));
			stmt.setString(3, DateUtils.formatDateSap(dataStop));
			stmt.setString(4, DateUtils.formatDateSap(dataStart));
			stmt.setString(5, DateUtils.formatDateSap(dataStop));

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				if (idDelegatie.toString().isEmpty())
					idDelegatie.append(rs.getString("id"));
				else {
					idDelegatie.append(", ");
					idDelegatie.append(rs.getString("id"));
				}
			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return idDelegatie.toString();

	}

	public void recalculeazaDelegatie(String idDelegatie) throws SQLException {

		Connection conn = new DBManager().getProdDataSource().getConnection();

		stergeLocalitatiAdaugate(conn, idDelegatie);
		new OperatiiTraseu().determinaSfarsitDelegatie(conn, idDelegatie);
		actualizeazaStareFaz(conn, idDelegatie);

		conn.close();

	}

	private void stergeLocalitatiAdaugate(Connection conn, String idDelegatie) {

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.stergeLocalitatiAdaugate())) {

			stmt.setString(1, idDelegatie);
			stmt.executeQuery();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

	}

	private void actualizeazaStareFaz(Connection conn, String idDelegatie) {

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.actualizeazaStareFaz())) {

			stmt.setString(1, idDelegatie);
			stmt.executeQuery();

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

	}

}
