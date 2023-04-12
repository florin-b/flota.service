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
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("ATR"))
			codAprobare = getCodAprobareATR(conn, delegatie);
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("SBL"))
			codAprobare = getCodAprobareSBA(conn, delegatie);
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("CVW"))
			codAprobare = getCodAprobareCVW(conn, delegatie.getCodAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("CVR"))
			codAprobare = getCodAprobareCVR(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("CVIP"))
			codAprobare = getCodAprobareCVIP(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("CVA"))
			codAprobare = getCodAprobareCVA(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("RGEST"))
			codAprobare = getCodAprobareRGEST(conn, delegatie.getCodAngajat(), delegatie.getTipAngajat());
		else if (delegatie.getTipAngajat().trim().equalsIgnoreCase("MP"))
			codAprobare = getCodAprobareMP(conn, delegatie.getCodAngajat());
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
		return delegatie.getUnitLog().contains("BU90") || delegatie.getUnitLog().contains("GL90") || delegatie.getUnitLog().contains("BV90");

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

				if (rs.getString("aprobat").equalsIgnoreCase("SM"))
					codSM = rs.getString("fid");
				else if (rs.getString("aprobat").equalsIgnoreCase("SMG"))
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

			if (codAprobare == null && tipKA.equals("KA"))
				codAprobare = "27";

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
			else {

				String codAprobare04 = getCodAprobareAV04(conn, codAngajat);
				if (!codAprobare04.isEmpty())
					codAprobare = codAprobare04;
				else
					codAprobare = codDZ;
			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	private static String getCodAprobareAV04(Connection conn, String codAgent) {

		String codAprobare = "";

		if (!isAgent04(conn, codAgent))
			return codAprobare;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareSD04())) {

			stmt.setString(1, codAgent);
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

	private static boolean isAgent04(Connection conn, String codAgent) {
		boolean isAg04 = false;

		String sqlString = "select 1 from agenti where cod=? and divizie like '04%' ";

		try (PreparedStatement stmt = conn.prepareStatement(sqlString)) {

			stmt.setString(1, codAgent);
			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				isAg04 = true;
			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return isAg04;

	}

	private static String getCodAprobareATR(Connection conn, DelegatieNoua delegatie) {
		String codAprobare = null;

		if (delegatie.getUnitLog().equals("GL90")) {
			codAprobare = "1";
			return codAprobare;
		}

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareATRFiliale())) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				codAprobare = rs.getString(1);
			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;
	}

	private static String getCodAprobareSBA(Connection conn, DelegatieNoua delegatie) {
		String codAprobare = null;

		String sqlString = "";

		if (delegatie.getUnitLog().equals("GL90")) {
			sqlString = SqlQueries.getCodAprobareSBAGLCentral();
		} else
			sqlString = SqlQueries.getCodAprobareSBAFiliale();

		try (PreparedStatement stmt = conn.prepareStatement(sqlString)) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				codAprobare = rs.getString(1);
			}

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;
	}

	public static String getCodAprobareCVW(Connection conn, String codAngajat) {

		String codAprobare = null;
		String codSMW = null;
		String codDZ = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareCVW());) {

			stmt.setString(1, codAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("SMW"))
					codSMW = rs.getString("fid");

				if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

			}

			if (codSMW != null)
				codAprobare = codSMW;
			else
				codAprobare = codDZ;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}

	public static String getCodAprobareCVIP(Connection conn, String codAngajat, String tipAngajat) {

		String codAprobare = null;
		
		String codSDIP = null;
		String codDZ = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareCVIP());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, tipAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("SDIP"))
					codSDIP = rs.getString("fid");
				
				if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

			}

			if (codSDIP != null)
				codAprobare = codSDIP;
			else
				codAprobare = codDZ;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}
	
	
	public static String getCodAprobareCVA(Connection conn, String codAngajat, String tipAngajat) {

		String codAprobare = null;
		String codSMG = null;
		String codDZ = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareCVA());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, tipAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("SMG"))
					codSMG = rs.getString("fid");

				if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

			}

			if (codSMG != null)
				codAprobare = codSMG;
			else
				codAprobare = codDZ;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}
	
	
	public static String getCodAprobareMP(Connection conn, String codAngajat){
		
		String codAprobare = null;
		String codDD = null;
		String codDLOG = null;
		String departAngajat = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareMP());) {

			stmt.setString(1, codAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				
				departAngajat = rs.getString("departament");

				if (rs.getString("aprobat").equalsIgnoreCase("DD"))
					codDD = rs.getString("fid");

				if (rs.getString("aprobat").equalsIgnoreCase("DLOG"))
					codDLOG = rs.getString("fid");

			}

			if (departAngajat != null)
				codAprobare = codDD;
			else
				codAprobare = codDLOG;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;
		
	}
	

	public static String getCodAprobareRGEST(Connection conn, String codAngajat, String tipAngajat) {

		String codAprobare = null;
		String codDZ = null;
		String codDAG = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareRGEST());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, tipAngajat);
			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				if (rs.getString("aprobat").equalsIgnoreCase("DZ"))
					codDZ = rs.getString("fid");

				if (rs.getString("aprobat").equalsIgnoreCase("DAG"))
					codDAG = rs.getString("fid");

			}

			if (codDZ != null)
				codAprobare = codDZ;
			else
				codAprobare = codDAG;

		} catch (SQLException e) {
			MailOperations.sendMail(e.toString());
			logger.error(Utils.getStackTrace(e));
		}

		return codAprobare;

	}	
	
	
	
	
	public static String getCodAprobareCVR(Connection conn, String codAngajat, String tipAngajat) {

		String codAprobare = null;

		try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.getCodAprobareCVR());) {

			stmt.setString(1, codAngajat);
			stmt.setString(2, tipAngajat);
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
	
	

}
