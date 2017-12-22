package flota.service.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import flota.service.beans.DelegatieNoua;
import flota.service.queries.SqlQueries;
import flota.service.utils.DateUtils;
import flota.service.utils.MailOperations;
import flota.service.utils.Utils;

public class HelperAprobare {

	private static final Logger logger = LogManager.getLogger(HelperAprobare.class);

	public static String getCodAprobare(Connection conn, String codAngajat, String tipAngajat) {

		String codAprobare;

		if (tipAngajat.toUpperCase().startsWith("KA"))
			codAprobare = getCodAprobareKA(conn, codAngajat, tipAngajat);
		else if (tipAngajat.toUpperCase().startsWith("CAG") || tipAngajat.toUpperCase().startsWith("CONS"))
			codAprobare = getCodAprobareConsilieri(conn, codAngajat, tipAngajat);
		else if (tipAngajat.trim().equalsIgnoreCase("CJ"))
			codAprobare = getCodAprobareJuridic(conn, codAngajat, tipAngajat.trim());
		else if (tipAngajat.trim().equalsIgnoreCase("AV"))
			codAprobare = getCodAprobareAV(conn, codAngajat);
		else
			codAprobare = getCodAprobareGeneral(conn, codAngajat);

		return codAprobare;

	}

	public static String getCodAprobare(Connection conn, DelegatieNoua delegatie) {

		String codAprobare;

		if (delegatie.getTipAngajat().toUpperCase().startsWith("KA"))
			codAprobare = getCodAprobareKA(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().toUpperCase().startsWith("CAG") || delegatie.getTipAngajat().toUpperCase().startsWith("CONS"))
			codAprobare = getCodAprobareConsilieri(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("CJ"))
			codAprobare = getCodAprobareJuridic(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("AV"))
			codAprobare = getCodAprobareAV(conn, delegatie.getCodAngajat());
		else
			codAprobare = getCodAprobareGeneral(conn, delegatie.getCodAngajat());

		String codAprobareWeekend = getCodAprobareWeekend(conn, delegatie);

		if (codAprobareWeekend != null)
			codAprobare = codAprobareWeekend;

		return codAprobare;

	}

	private static String getCodAprobareWeekend(Connection conn, DelegatieNoua delegatie) {

		String codAprobareWeekend = null;

		boolean hasWeekend = DateUtils.hasWeekend(delegatie.getDataP(), delegatie.getDataS());

		if (hasWeekend && !isUlCentral(delegatie)) {
			codAprobareWeekend = getCodAprobareDZ(conn);
		}

		return codAprobareWeekend;
	}

	private static boolean isUlCentral(DelegatieNoua delegatie) {
		return delegatie.getUnitLog().equals("BU90") || delegatie.getUnitLog().equals("GL90") || delegatie.getUnitLog().equals("BV90");

	}

	public static String getCodAprobareDZ(Connection conn) {
		String codAprobare = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareDZ());) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				codAprobare = rs.getString("fid");

			}

			rs.close();

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(Utils.getStackTrace(e));

		}

		return codAprobare;
	}

	public static String getCodAprobareGeneral(Connection conn, String codAngajat) {

		String codAprobare = "-1";

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobare());) {

			stmt.setString(1, codAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				codAprobare = rs.getString("fid");

			}

		} catch (SQLException e) {
			logger.error(Utils.getStackTrace(e));
			MailOperations.sendMail(Utils.getStackTrace(e));

		}

		return codAprobare;

	}

	public static String getCodAprobareConsilieri(Connection conn, String codConsilier, String tipConsilier) {

		String codAprobare = null;
		String codSM = null;
		String codSDCVA = null;
		String codDZ = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareConsilieri());) {

			String tipCons = tipConsilier;

			if (tipConsilier.equals("CONS_GED"))
				tipCons = "CONS-GED";

			stmt.setString(1, codConsilier);
			stmt.setString(2, tipCons);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("SMG"))
					codSM = rs.getString("fid");
				else if (rs.getString("aprobat").equalsIgnoreCase("SDCVA"))
					codSDCVA = rs.getString("fid");
				else if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

			}

			if (codSM != null)
				codAprobare = codSM;

			if (codSDCVA != null)
				codAprobare = codSDCVA;

			if (codSM == null && codSDCVA == null)
				codAprobare = codDZ;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	public static String getCodAprobareKA(Connection conn, String codAngajat, String tipKA) {

		String codAprobare = null;
		String codSDKA = null;
		String codDZ = null;

		if (tipKA.equals("KA08"))
			return getCodAprobareKA08(conn);

		if (tipKA.equals("KA05"))
			return getCodAprobareKA05(conn);

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareKA());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, tipKA);

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("SDKA"))
					codSDKA = rs.getString("fid");
				else if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

			}

			if (codSDKA != null)
				codAprobare = codSDKA;
			else
				codAprobare = codDZ;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	public static String getCodAprobareKA08(Connection conn) {

		String codAprobare = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareKA08());) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				codAprobare = rs.getString("fid");

			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	public static String getCodAprobareJuridic(Connection conn, String codAngajat, String tipAngajat) {

		String codAprobare = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareJuridic());) {

			stmt.setString(1, tipAngajat);
			stmt.setString(2, tipAngajat);
			stmt.setString(3, codAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				codAprobare = rs.getString("fid");

			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	public static String getCodAprobareKA05(Connection conn) {

		String codAprobare = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareKA05());) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				codAprobare = rs.getString("fid");

			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	public static String getCodAprobareAV(Connection conn, String codAngajat) {

		String codAprobare = null;
		String codSD = null;
		String codDZ = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareAV());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, codAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("SD"))
					codSD = rs.getString("fid");

				if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

			}

			if (codSD != null)
				codAprobare = codSD;
			else
				codAprobare = codDZ;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

}
