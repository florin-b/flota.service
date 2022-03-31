package flota.service.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.maps.model.LatLng;

import flota.service.beans.BeanDelegatieCauta;
import flota.service.beans.DelegatieNoua;
import flota.service.beans.PunctTraseu;
import flota.service.database.DBManager;
import flota.service.helpers.HelperAprobare;
import flota.service.model.OperatiiDelegatii;
import flota.service.model.OperatiiTraseu;
import flota.service.model.ServiceDelegatii;
import flota.service.utils.MapUtils;

public class TestClass {

	private static final Logger logger = LogManager.getLogger(TestClass.class);

	public static void main(String[] args) throws SQLException {

		//new OperatiiDelegatii().recalculeazaDelegatie("159960248830");
		
		// System.out.println(new OperatiiAngajat().getAngajatCategorie("MS10",
		// "
		// ARC;ATR;AV;CAG;CAG1;CAG2;CAG3;CJ;CONS-GED;CVG;CVR;CVW;DADMIN;DZ;GD;GS;IHR;IOFR08;IOFR09;ISSM;KA;KA08;KA1;KA1;KA2;KA3;MAC;MM;MMPROD;OC;OIVPD;RGEST;SBA;SBAL;SBL;SD;SDCVA;SDKA;SHR;SM;SMG;SMR;SMW;SOF;SSFC;SSPROD;STIV;
		// ",
		// ""));

		//System.out.println("De aprobat:" + new OperatiiDelegatii().getDelegatiiAprobari("DLOG", "GL90", ""));
		
		//System.out.println("De aprobat:" + new OperatiiDelegatii().getDelegatiiAprobari("SMG", "BU10", "11"));

		//new ServiceDelegatii().calculeazaKmSfarsitLuna();

		// new TestClass().accident();

		// testRecalculeaza();

		// testNrOpriri();

		// System.out.println("Del:" + new
		// OperatiiDelegatii().afiseazaDelegatiiProprii("00000475",
		// "29-05-2019", "04-06-2019"));

		// System.out.println("Modif del: " + new
		// OperatiiDelegatii().getDelegatiiModificare("00000475"));

		
		  List<LatLng> coords = new ArrayList<LatLng>();
		  
		  coords.add(new LatLng(45.473149, 27.936426));
		  
		  
		  coords.add(new LatLng(45.785878, 27.802911));
		  
		  System.out.println("REs: " + MapUtils.getAdreseCoordonate(coords));
		 

		// System.out.println("Stare gps: " + new
		// OperatiiMasina().getStareGps("00083045"));

		// adaugaDelegatie DelegatieNoua [codAngajat=00077313, tipAngajat=CVR,
		// dataP=21-10-2019, oraP=0500, dataS=21-10-2019, distcalc=21,
		// stops=GALATI / GALATI,BRAILA / BRAILA, nrAuto=GL-09-RRG, distreal=0,
		// unitLog=BU90, id=null]

		/*
		DelegatieNoua delegatie = new DelegatieNoua();

		delegatie.setCodAngajat("00017858");
		delegatie.setTipAngajat("RGEST");
		delegatie.setDataP("21-08-2020");
		delegatie.setOraP("0500");
		delegatie.setDataS("21-08-2020");
		delegatie.setNrAuto("GL-40-EDI");
		delegatie.setDistcalc("21");
*/
		//getCodAprobare();

		//new OperatiiDelegatii().adaugaDelegatie(delegatie);
		
		
		//MailOperations.sendMail("Flota JOB", "Start");
		//new OperatiiDelegatii().verificaDelegatiiTerminateCompanie();
		//MailOperations.sendMail("Flota JOB", "Stop");
		
		//testRegex();
		

	}
	
	public static void testRegex(){
		
		String expression = " Client 123 (CT! "; 
				
		String val = expression.replaceFirst("\\(.*", "");
		
		System.out.println(val);
	}

	public static void getCodAprobare() {

		try (Connection conn = new DBManager().getProdDataSource().getConnection();) {

			System.out.println("Cod aprobare: " + HelperAprobare.getCodAprobareCVIP(conn, "00080378","CVIP"));

		} catch (SQLException e) {

		}

	}

	private static void recalculeazaDelegatii() {

		String sqlString = "select id, distcalc, distreal, distreal/distcalc from sapprd.zdelegatiehead "
				+ " where data_sosire >= '20171120' and data_sosire < '20171128' and distcalc > 0 and distreal/distcalc >=0.7 and "
				+ " distreal/distcalc <=0.9 and rownum<300 ";

		try (Connection conn = new DBManager().getProdDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sqlString)) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			OperatiiDelegatii opDelegatii = new OperatiiDelegatii();

			while (rs.next()) {
				System.out.println("Calculare delegatie " + rs.getString("id"));

				opDelegatii.recalculeazaDelegatie(rs.getString("id"));
			}

		} catch (SQLException e) {

			System.out.println(e);
		}

	}

	private void accident() {

		String sqlString = "   select * from nexus_gps_data where trunc(gtime) ='06-02-2019' and " + " vcode in (select  v.vcode "
				+ " from sapprd.anlz a join sapprd.anla b on b.anln1 = a.anln1 and b.anln2 = a.anln2 and b.mandt=a.mandt "
				+ " join sapprd.aufk c on c.aufnr = a.caufn and c.mandt=a.mandt "
				+ " join nexus_vehicles v on replace(replace(v.car_number,' ',''),'-','') = replace(replace(c.ktext,' ',''),'-','') " + " where a.pernr in "
				+ " (select cod from personal where filiala like 'BV%') " + " and a.bdatu >= (select to_char(sysdate-5,'YYYYMMDD') from dual) and "
				+ " b.deakt = '00000000' and a.mandt='900' and c.auart = '2001' ) ";

		Set<String> setCodes = new TreeSet<>();

		List<String> vCodes = new ArrayList<>();

		try (Connection conn = new DBManager().getProdDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sqlString)) {

			stmt.executeQuery();

			ResultSet rs = stmt.getResultSet();

			LatLng strada = new LatLng(45.663970, 25.601354);

			while (rs.next()) {

				LatLng punct = new LatLng(Double.valueOf(rs.getString("lat")), Double.valueOf(rs.getString("lon")));

				double distStrada = MapUtils.distanceXtoY(punct.lat, punct.lng, strada.lat, strada.lng, "K");

				if (distStrada < 0.1)
					setCodes.add(rs.getString("vcode"));

			}

			System.out.println(setCodes);

		} catch (SQLException e) {

			System.out.println(e);
		}

	}

	public static void testRecalculeaza() {

		// 18-04-2019 00:00 , 18-04-2019 23:59 , 45.185560,26.797024 ,
		// 45.185571,26.797096

		List<PunctTraseu> listPuncte = new ArrayList<>();

		BeanDelegatieCauta del = new BeanDelegatieCauta();
		del.setId("72428033764");
		del.setAngajatId("00083399");
		del.setDataPlecare("18-04-2019");
		del.setOraPlecare("0700");
		del.setDataSosire("18-04-2019");
		del.setDistantaCalculata(17);

		PunctTraseu punct = new PunctTraseu();
		punct.setPozitie(1);
		punct.setCoordonate(new LatLng(45.187130, 26.793235));
		punct.setAdresa(null);
		punct.setStrAdresa("BUZAU / CAPATANESTI");
		punct.setVizitat(false);
		listPuncte.add(punct);

		punct = new PunctTraseu();
		punct.setPozitie(2);
		punct.setCoordonate(new LatLng(45.137111, 26.817112));
		punct.setAdresa(null);
		punct.setStrAdresa("BUZAU / BUZAU");
		punct.setVizitat(true);
		listPuncte.add(punct);

		punct = new PunctTraseu();
		punct.setPozitie(3);
		punct.setCoordonate(new LatLng(45.187130, 26.793235));
		punct.setAdresa(null);
		punct.setStrAdresa("BUZAU / CAPATANESTI");
		punct.setVizitat(false);
		listPuncte.add(punct);

		try {
			Connection conn = new DBManager().getProdDataSource().getConnection();

			new OperatiiTraseu().recalculeazaTraseuTeoretic(conn, del, listPuncte);

			conn.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

	}

	private static void testNrOpriri() {

		try {
			Connection conn = new DBManager().getProdDataSource().getConnection();

			List<LatLng> coordonateOpriri = new OperatiiTraseu().getCoordOpririDelegatie(conn, "70486767680", "26.03.2019 00:00", "26.03.2019 23:59");

			System.out.println("Size= " + coordonateOpriri.size());

			conn.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

	}

}
