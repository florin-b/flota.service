package flota.service.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flota.service.beans.Angajat;
import flota.service.beans.AngajatCategorie;
import flota.service.beans.CategorieAngajat;
import flota.service.beans.Distanta;
import flota.service.database.DBManager;
import flota.service.queries.SqlQueries;
import flota.service.utils.DateUtils;
import flota.service.utils.MailOperations;
import flota.service.utils.Utils;

public class OperatiiAngajat {

	private static final Logger logger = LogManager.getLogger(OperatiiAngajat.class);

	public int getKmCota(Connection conn, String codAngajat, String dataStart, String dataStop) {

		int kmCota = 0;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getKmCota());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));
			stmt.setString(3, DateUtils.formatDateSap(dataStart));
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				kmCota = Integer.parseInt(rs.getString("cotakm"));
			}

			if (kmCota > 0)
				kmCota = getKmCotaInterval(kmCota, dataStart, dataStop);

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return kmCota;

	}

	private int getKmCotaInterval(int kmCota, String startDate, String stopDate) {

		int totalKm = 0;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		LocalDate start = LocalDate.parse(startDate, formatter);
		LocalDate end = LocalDate.parse(stopDate, formatter);

		LocalDate next = start.minusDays(1);
		while ((next = next.plusDays(1)).isBefore(end.plusDays(1))) {
			if (next.getDayOfWeek() != DayOfWeek.SUNDAY)
				totalKm += kmCota;

		}

		return totalKm;

	}

	public List<Angajat> getAngajati(String tipAngajat, String unitLog, String codDepart, String codAngajat) {

		List<Angajat> listAngajati = new ArrayList<>();

		boolean isPersVanzari = Utils.isAngajatVanzari(tipAngajat);

		String sqlString;

		String unitLogQs = Utils.generateQs(unitLog);
		String departQs = Utils.generateQs(codDepart);

		if (codAngajat.equals("00018768")) {
			sqlString = SqlQueries.getConsilieriIP();
		} else if (isPersVanzari) {
			sqlString = SqlQueries.getSubordVanzari(unitLogQs, departQs);
		} else {
			if (tipAngajat.equals("DJ"))
				sqlString = SqlQueries.getSubordDirectorJuridic();
			else
				sqlString = SqlQueries.getSubordNonVanzari(unitLogQs);
		}

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			int pos = 3;
			String[] unitLogs = unitLog.split(",");
			String[] departs = codDepart.split(",");

			if (!codAngajat.equals("00018768")) {
			stmt.setString(1, tipAngajat);
			stmt.setString(2, codAngajat);
			}

			if (!tipAngajat.equals("DJ") && !codAngajat.equals("00018768")) {

				for (int ii = 0; ii < unitLogs.length; ii++)
					stmt.setString(pos++, unitLogs[ii]);

				if (isPersVanzari) {

					for (int ii = 0; ii < departs.length; ii++)
						stmt.setString(pos++, departs[ii].substring(0, 2));
				}
			}

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				Angajat angajat = new Angajat();

				angajat.setCod(rs.getString("cod"));
				angajat.setNume(rs.getString("nume"));

				listAngajati.add(angajat);
			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return listAngajati;
	}

	public List<CategorieAngajat> getCategSubord(String tipAngajat) {
		List<CategorieAngajat> categorii = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCategoriiSubordonati())) {

			stmt.setString(1, tipAngajat);
			stmt.setString(2, tipAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				CategorieAngajat categorie = new CategorieAngajat();
				categorie.setTip(rs.getString("cod"));
				categorie.setDescriere(rs.getString("descriere"));
				categorii.add(categorie);

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return categorii;
	}

	public List<AngajatCategorie> getAngajatCategorie(String filiala, String tipAngajat, String departament) {

		List<AngajatCategorie> listAngajati = new ArrayList<>();

		String tipAngajati = Utils.generateQs(tipAngajat.replace(';', ','));

		String unitLogQs = Utils.generateQs(filiala);

		String sqlString = "";

		boolean isCategorieVanzari;

		if (departament == null || departament.isEmpty() || tipAngajat.equals("DZ"))
			isCategorieVanzari = false;
		else
			isCategorieVanzari = Integer.parseInt(departament) >= 1 && Integer.parseInt(departament) <= 11;

		if (isCategorieVanzari)
			sqlString = SqlQueries.getAngajatiCategorieVanzari(unitLogQs, tipAngajati);
		else
			sqlString = SqlQueries.getAngajatiCategorieNonVanzari(unitLogQs, tipAngajati);

		try (Connection conn = new DBManager().getProdDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sqlString)) {

			String[] tipAng = tipAngajat.replace(';', ',').split(",");
			String[] unitLogs = filiala.split(",");

			int pos = 1;

			for (int ii = 0; ii < unitLogs.length; ii++)
				stmt.setString(pos++, unitLogs[ii]);

			for (int ii = 0; ii < tipAng.length; ii++)
				stmt.setString(pos++, tipAng[ii]);

			if (isCategorieVanzari)
				stmt.setString(pos++, departament.substring(0, 2));

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				AngajatCategorie angajat = new AngajatCategorie();

				angajat.setCod(rs.getString("cod"));
				angajat.setNume(rs.getString("nume"));
				angajat.setCategorie(rs.getString("functie"));
				listAngajati.add(angajat);

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return listAngajati;
	}

	public String getNumeAngajat(Connection conn, String codAngajat) {

		String numeAngajat = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getNumeAngajat())) {

			stmt.setString(1, codAngajat);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				numeAngajat = rs.getString("nume");
			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return numeAngajat;
	}

	public List<String> getAngajatiCuDelegatii(String data) {
		List<String> listAngajati = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getAngajatiCuDelegatii())) {

			stmt.setString(1, data);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				listAngajati.add(rs.getString("codangajat"));

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return listAngajati;
	}

	public static String getAngajatGps(Connection conn, String data, String codGps) {

		String codAngajat = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getAngajatGps())) {

			stmt.setString(1, data);
			stmt.setString(2, data);
			stmt.setString(3, codGps);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				codAngajat = rs.getString("pernr");

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return codAngajat;
	}

	public static boolean isAngajatLiberKm(Connection conn, String data, String codAngajat) {

		boolean isLiber = false;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.isAngajatLiberKm())) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, data);
			stmt.setString(3, data);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				isLiber = rs.getString("liber").equalsIgnoreCase("X");
			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return isLiber;
	}

	public int getKmPrag(Connection conn, String codAngajat, String data) {
		int kmPrag = 0;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getKmPrag())) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, data);
			stmt.setString(3, data);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				kmPrag = rs.getInt("km_prag");

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return kmPrag;
	}

	public void getAngajatiFaraDelegatii(List<String> angajatiCuDelegatii, List<Distanta> distanteParcurse) {

		try (Connection conn = new DBManager().getProdDataSource().getConnection();) {

			Iterator<Distanta> iterator = distanteParcurse.iterator();

			String codAngajat;
			boolean isAngajatLiberKm;
			while (iterator.hasNext()) {
				Distanta dist = iterator.next();

				codAngajat = getAngajatGps(conn, DateUtils.getYesterday(), dist.getCodDisp());
				dist.setCodAngajat(codAngajat);

				isAngajatLiberKm = isAngajatLiberKm(conn, DateUtils.getYesterday(), codAngajat);

				if (angajatiCuDelegatii.contains(codAngajat) || isAngajatLiberKm)
					iterator.remove();

			}

			int kmPrag = 0;
			iterator = distanteParcurse.iterator();
			while (iterator.hasNext()) {
				Distanta dist = iterator.next();

				kmPrag = getKmPrag(conn, dist.getCodAngajat(), DateUtils.getYesterday());

				if (dist.getDistanta() < kmPrag)
					iterator.remove();

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

	}

	public static String getAdresaMailAngajat(Connection conn, String codAngajat) {

		String adresaMail = "";

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getAdresaMailAngajat())) {

			stmt.setString(1, codAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				adresaMail = rs.getString("mail");

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return adresaMail;
	}

	public void sendMailAlerts(List<Distanta> listDistante, String data) {

		try (Connection conn = new DBManager().getProdDataSource().getConnection()) {

			String adresaMail = "";
			String textMail = "";
			String nrAuto = "";

			OperatiiMasina opMasina = new OperatiiMasina();
			for (Distanta distanta : listDistante) {

				adresaMail = getAdresaMailAngajat(conn, distanta.getCodAngajat());
				nrAuto = opMasina.getNrAutoCodGps(conn, distanta.getCodDisp());
				textMail = "\n";

				textMail += "In data de " + data + " ati efectuat " + distanta.getDistanta() + " km (" + nrAuto + ") si nu aveti delegatie creata in sistem. ";
				textMail += "\n";
				textMail += "Daca trebuia sa o intocmiti si nu ati facut-o va rugam sa o creati in cel mai scurt timp.";
				textMail += "\n\n";
				textMail += "\n\n";

				MailOperations.sendMailNotificare(adresaMail, textMail);

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

	}

}
