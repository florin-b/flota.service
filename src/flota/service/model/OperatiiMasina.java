package flota.service.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flota.service.beans.Distanta;
import flota.service.beans.StareGps;
import flota.service.database.DBManager;
import flota.service.enums.EnumStareGps;
import flota.service.queries.SqlQueries;
import flota.service.utils.DateUtils;
import flota.service.utils.MailOperations;
import flota.service.utils.Utils;

public class OperatiiMasina {

	private static final Logger logger = LogManager.getLogger(OperatiiMasina.class);

	public String getCodDispGps(Connection conn, String nrDelegatie) {

		String codDisp = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodDispGps(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, nrDelegatie);

			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				codDisp = rs.getString("vcode");

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return codDisp;

	}

	public List<String> getCodDispGps(String codAngajat, String dataStart) {

		List<String> listDisp = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodDispGpsData())) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));

			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				listDisp.add(rs.getString("vcode"));
			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}
		return listDisp;

	}

	public String getNrAuto(String nrDelegatie) {

		String nrAuto = null;

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getNrAuto(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			stmt.setString(1, nrDelegatie);

			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				nrAuto = rs.getString("nrauto");
			}

		}

		catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return nrAuto;

	}

	public List<String> getMasiniAngajat(String codAngajat, String dataStart) {

		List<String> nrAuto = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getMasiniAngajatData());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));

			
			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				nrAuto.add(rs.getString("ktext"));
			}

		}

		catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return nrAuto;

	}

	public List<String> getMasiniAngajat(Connection conn, String codAngajat) {

		List<String> nrAuto = new ArrayList<>();

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getMasiniAngajat());) {

			stmt.setString(1, codAngajat);

			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				nrAuto.add(rs.getString("ktext").replace("-", "").replace(" ", ""));
			}

		}

		catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return nrAuto;

	}

	public String getCodGps(String codAngajat, String dataStart) {

		StringBuilder codes = new StringBuilder();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(SqlQueries.getMasiniAlocateData());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));

			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				if (codes.toString().isEmpty())
					codes.append(rs.getString("vcode"));
				else {
					codes.append(rs.getString(","));
					codes.append(rs.getString("vcode"));
				}

			}

		}

		catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return codes.toString();

	}

	public String getCodGps(Connection connection, String codAngajat, String dataStart) {

		StringBuilder codes = new StringBuilder();

		try (PreparedStatement stmt = connection.prepareStatement(SqlQueries.getMasiniAlocateData());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, DateUtils.formatDateSap(dataStart));

			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				if (codes.toString().isEmpty()) {
					codes.append(rs.getString("vcode"));
					break;
				}

			}

		}

		catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return codes.toString();

	}

	public String getNrAutoCodGps(Connection conn, String codDispGps) {

		String nrAuto = " ";

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getNrAutoCodGps());) {

			stmt.setString(1, codDispGps);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				nrAuto = rs.getString("car_number");

			}

		}

		catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(e.toString());
		}

		return nrAuto;

	}

	public List<Distanta> getDistante(String data) {
		List<Distanta> listDistante = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(SqlQueries.getDistante())) {

			stmt.setString(1, data);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				Distanta dist = new Distanta();
				dist.setCodDisp(rs.getString(1));
				dist.setDistanta((int) rs.getDouble(2));
				listDistante.add(dist);

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
		}

		return listDistante;
	}

	public StareGps getStareGps(String codAngajat) {

		StareGps stareGps = new StareGps();

		try (Connection conn = new DBManager().getProdDataSource().getConnection();) {

			List<String> listAuto = new OperatiiMasina().getMasiniAngajat(conn, codAngajat);

			if (!listAuto.isEmpty()) {

				stareGps.setNrAuto(listAuto.get(0));

				try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getStareGps())) {

					stmt.setString(1, listAuto.get(0));
					stmt.executeQuery();

					ResultSet rs = stmt.getResultSet();

					while (rs.next()) {
						stareGps.setData(rs.getString(1));
						stareGps.setStareGps(rs.getDouble(2) > 0 ? EnumStareGps.BUSINESS : EnumStareGps.PERSONAL);

					}

				}

			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return stareGps;

	}

}
