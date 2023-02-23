package flota.service.queries;

public class SqlQueries {
	public static String getLocalitatiJudet() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append("select upper(localitate) localitate from sapprd.zlocalitati where bland=? order by localitate");

		return sqlString.toString();
	}

	public static String getListLocalitati() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append("select upper(localitate) localitate, bland from sapprd.zlocalitati where lower(localitate) like '?%' order by localitate");

		return sqlString.toString();
	}

	public static String adaugaAntetDelegatie() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" insert into sapprd.zdelegatiehead(mandt, id, codangajat, datac, orac, data_plecare, ora_plecare, ");
		sqlString.append(" distcalc, distrespins, idaprob, data_sosire, nrAuto, distreal) ");
		sqlString.append(" values ('900',?,?,?,?,?,?,?,0,?,?,?,?) ");

		return sqlString.toString();
	}

	public static String adaugaOpririDelegatie() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" insert into sapprd.zdelegatieruta(mandt, id, poz, judet, localitate, vizitat, init) ");
		sqlString.append(" values ('900',?,?,?,?,?,?) ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareHeaderVanzari_old(String unitLogQs, String departQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id, h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, ");
		sqlString.append(" h.distrespins, h.data_sosire, h.distreal, h.distrecalc, f.cod codAprob  ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag, functii_non_vanzari f  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and h.idaprob = f.fid ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and substr(ag.departament,0,2) in ");
		sqlString.append(departQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareHeaderVanzariSDIP(String unitLogQs, String departQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id, h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, ");
		sqlString.append(" h.distrespins, h.data_sosire, h.distreal, h.distrecalc, f.cod codAprob  ");
		sqlString.append(" from sapprd.zdelegatiehead h, agenti ag, functii_non_vanzari f  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=?) ");
		sqlString.append(" and h.idaprob = f.fid ");
		sqlString.append(" and ag.centru_cost in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareHeaderVanzari(String unitLogQs, String departQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id, h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, ");
		sqlString.append(" h.distrespins, h.data_sosire, h.distreal, h.distrecalc, f.cod codAprob  ");
		sqlString.append(" from sapprd.zdelegatiehead h, agenti ag, functii_non_vanzari f  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and h.idaprob = f.fid ");
		sqlString.append(" and ag.centru_cost in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and substr(ag.divizie,0,2) in ");
		sqlString.append(departQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareHeaderNONVanzari(String unitLogQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id, h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, ");
		sqlString.append(" h.distrespins, h.data_sosire, h.distreal, h.distrecalc, f.cod codAprob ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag, functii_non_vanzari f  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and h.idaprob = f.fid ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" and ag.functie in ( select cod from functii_non_vanzari where aprobat =? ) ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareHeaderNONVanzari_DAG(String unitLogQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id , h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, ");
		sqlString.append(" h.distrespins, h.data_sosire, h.distreal, h.distrecalc, f.cod codAprob ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag, functii_non_vanzari f  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and h.idaprob = f.fid ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" and ag.functie in ( select cod from functii_non_vanzari where aprobat =? ) ");
		sqlString.append(" union "); // delegatii Durlai, DP + DINV
		sqlString.append(" select h.id , h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc,  h.distrespins, h.data_sosire, ");
		sqlString.append(" h.distreal, h.distrecalc, 'DP' codAprob  from sapprd.zdelegatiehead h, personal ag  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and h.codangajat = '00140436' and h.codangajat = ag.cod  ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareHeaderNONVanzari_DZ(String unitLogQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id, h.distreal, h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, ");
		sqlString.append(" h.distrespins, h.data_sosire, h.distreal, h.distrecalc, f.cod codAprob ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag, functii_non_vanzari f  where h.mandt='900' and ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd')>= to_date(sysdate - 45) and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and h.idaprob = f.fid ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String getDelegatiiTerminateVanzari(String unitLogQs) {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id ");
		sqlString.append(" from sapprd.zdelegatiehead h, agenti ag where h.mandt='900' and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and ag.centru_cost in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and substr(ag.divizie,0,2)=? and h.codangajat = ag.cod ");
		sqlString.append(" and not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = h.id and status in ('2','6')) ");
		sqlString.append(" and h.data_sosire < to_char(sysdate,'yyyymmdd') and distreal = 0  ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();

	}

	public static String getDelegatiiTerminateNONVanzari_DAG(String unitLogQs) {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and  ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" and not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = h.id and status in ('2','6')) ");
		sqlString.append(" and h.data_sosire < to_char(sysdate,'yyyymmdd') and distreal = 0   ");
		sqlString.append(" union ");
		sqlString.append(" select h.id from sapprd.zdelegatiehead h, personal ag where h.mandt='900' ");
		sqlString.append(" and h.codangajat = ag.cod and h.codangajat = '00140436' and ");
		sqlString.append(" not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = h.id and status in ('2','6')) ");
		sqlString.append(" and h.data_sosire < to_char(sysdate,'yyyymmdd') and distreal = 0");

		return sqlString.toString();

	}

	public static String getDelegatiiTerminateNONVanzari(String unitLogQs) {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and  ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=? or cod='WKND') ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.codangajat = ag.cod ");
		sqlString.append(" and not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = h.id and status in ('2','6')) ");
		sqlString.append(" and h.data_sosire < to_char(sysdate,'yyyymmdd') and distreal = 0   ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();

	}

	public static String getDelegatiiTerminateCompanie() {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id ");
		sqlString.append(" from sapprd.zdelegatiehead h where h.mandt='900' and ");
		sqlString.append(" not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = h.id and status in ('2','6')) ");
		sqlString.append(" and h.data_sosire < to_char(sysdate,'yyyymmdd') and distreal = 0  ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();

	}

	public static String afiseazaDelegatiiProprii() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id,  h.codangajat, h.data_plecare, h.ora_plecare, h.distcalc, h.distrespins, h.data_sosire, h.distreal, h.distrecalc, ");
		sqlString.append(" nvl((select status from sapprd.zdelstataprob where iddelegatie = h.id and rownum=1),'-1') status ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and h.codangajat = ag.cod and ");
		sqlString.append(" h.codangajat = ? and h.datac between ? and ? ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String afiseazaDelegatiiSubordVanzari(String unitLogQs, String departQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(
				" select h.id,  h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, h.distrespins, h.data_sosire, h.distreal, h.distrecalc ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and h.codangajat = ag.cod  and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=?) ");
		sqlString.append(" and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and substr(ag.departament,0,2) in ");
		sqlString.append(departQs);
		sqlString.append(" and h.datac between ? and ? ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String afiseazaDelegatiiSubordDirectorJuridic() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id,  h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, h.distrespins, ");
		sqlString.append(" h.data_sosire, h.distreal, h.distrecalc ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and h.codangajat = ag.cod and ");
		sqlString.append(" ag.functie in (select cod from functii_non_vanzari where aprobat='DJ')  ");
		sqlString.append(" and h.datac between ? and ? ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}
	
	
	public static String afiseazaDelegatiiSubordDirectorLogistica() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select h.id,  h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, h.distrespins, ");
		sqlString.append(" h.data_sosire, h.distreal, h.distrecalc ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and h.codangajat = ag.cod and ");
		sqlString.append(" ag.functie in (select cod from functii_non_vanzari where aprobat='DLOG')  ");
		sqlString.append(" and h.datac between ? and ? ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String afiseazaDelegatiiSubordNONVanzari(String unitLogQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(
				" select h.id,  h.codangajat, h.data_plecare, h.ora_plecare, ag.nume, h.distcalc, h.distrespins, h.data_sosire, h.distreal, h.distrecalc ");
		sqlString.append(" from sapprd.zdelegatiehead h, personal ag where h.mandt='900' and h.codangajat = ag.cod and ");
		sqlString.append(" h.idaprob in (select fid from functii_non_vanzari where aprobat=?)  and ag.filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and h.datac between ? and ? ");
		sqlString.append(" order by h.id ");

		return sqlString.toString();
	}

	public static String getDelegatieStatus() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select x.status from ( ");
		sqlString.append(" select status from sapprd.zdelstataprob where iddelegatie=? order by to_date(dataaprob||' '||oraaprob,'yyyymmdd hh24miss') desc ");
		sqlString.append(" ) x where rownum=1  ");

		return sqlString.toString();
	}

	public static String getDelegatiiAprobareRuta() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select r.judet, r.localitate, r.vizitat, r.init from sapprd.zdelegatieruta r where r.id = ?  order by to_number(r.poz)  ");
		return sqlString.toString();
	}

	public static String getPuncteTraseu() {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select r.judet, r.localitate, r.vizitat , ");
		sqlString.append(" nvl( (select  nvl(l.latitudine,'-1') from sapprd.zcoordlocalitati l where ");
		sqlString.append(" trim(r.judet) = trim(l.judet) and trim(r.localitate) = trim(l.localitate) and rownum=1),'-1') lat, ");
		sqlString.append(" nvl((select  nvl(l.longitudine,'-1') from sapprd.zcoordlocalitati l where ");
		sqlString.append(" trim(r.judet) = trim(l.judet) and trim(r.localitate) = trim(l.localitate) and rownum=1),'-1') lon, ");
		sqlString.append(" r.poz from ");
		sqlString.append(" sapprd.zdelegatieruta r where r.id = ? ");
		sqlString.append(" order by to_number(r.poz) ");

		return sqlString.toString();

	}

	public static String opereazaDelegatie() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(
				" insert into sapprd.zdelstataprob (mandt, iddelegatie, tipaprob, status, dataaprob, oraaprob, codAngajat) values ('900',?,?,?,?,?,?) ");
		return sqlString.toString();
	}

	public static String aprobaAutomatDelegatie() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" insert into sapprd.zdelstataprob (mandt, iddelegatie, tipaprob, status, dataaprob, oraaprob) values ");
		sqlString.append(" ('900',?,'AUTO','2',?,?) ");
		return sqlString.toString();
	}

	public static String setKmRespinsi() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append("update sapprd.zdelegatiehead set distrespins=? where id = ?");
		return sqlString.toString();

	}

	public static String getCoordonateTraseu() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select x.* from (select rownum idt, to_char(gtime,'HH24:mi') gtime, lat, lon, speed, km from nexus_gps_data ");
		sqlString.append(" where vcode in ");
		sqlString.append(" (select vcode from our_vehicles n, sapprd.zdelegatiehead z where ");
		sqlString.append(" trim(regexp_replace(n.car_number,'-| ','')) = trim(regexp_replace(z.nrauto,'-| ','')) and z.id =? ) ");
		sqlString.append(" and gtime between to_date(?,'dd-mm-yyyy HH24:mi') ");
		sqlString.append(" and to_date(?,'dd-mm-yyyy HH24:mi')  ) x where remainder(x.idt,2) = 0 order by x.km ");

		return sqlString.toString();
	}

	public static String getCoordRuta() {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(
				" select x.* from (select rownum idt, to_char(d.gtime,'dd-mm-yyyy HH24:mi') gtime, d.lat, d.lon, d.speed, d.km from nexus_gps_data d, ");
		sqlString.append(" our_vehicles n ");
		sqlString.append(" where d.vcode = n.vcode  and ");
		sqlString.append(" trim(regexp_replace(n.car_number,'-| ','')) = trim(regexp_replace(?,'-| ','')) and ");
		sqlString.append(" trunc(d.gtime) between to_date(?,'dd-mm-yyyy HH24:mi') and ");
		sqlString.append(" to_date(?,'dd-mm-yyyy HH24:mi')  ) x where remainder(x.idt,1) = 0 order by x.gtime ");

		return sqlString.toString();

	}

	public static String getCodDispGps() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select vcode from our_vehicles n, sapprd.zdelegatiehead z where ");
		sqlString.append(" trim(regexp_replace(n.car_number,'-| ','')) = trim(regexp_replace(z.nrauto,'-| ','')) and z.id =?  ");

		return sqlString.toString();

	}

	public static String getCodDispGpsData() {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct vcode from our_vehicles n where ");
		sqlString.append(" trim(regexp_replace(n.car_number,'-| ','')) in ");
		sqlString.append(" ( select  trim(regexp_replace(c.ktext,'-| ','')) nrauto ");
		sqlString.append(" from sapprd.anlz a join sapprd.anla b on b.anln1 = a.anln1 and b.anln2 = a.anln2 and b.mandt=a.mandt ");
		sqlString.append(" join sapprd.aufk c on c.aufnr = a.caufn and c.mandt=a.mandt ");
		sqlString.append(" where a.pernr =? ");
		sqlString.append(" and a.bdatu >= ? and b.deakt = '00000000' and a.mandt='900') ");

		return sqlString.toString();
	}

	public static String getNrAuto() {

		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select nrauto from sapprd.zdelegatiehead where id =? ");
		return sqlString.toString();

	}

	public static String getDelegatieCauta() {

		StringBuilder sqlString = new StringBuilder();
		sqlString.append("select data_plecare, ora_plecare, data_sosire, distcalc, codangajat from sapprd.zdelegatiehead where id = ?");
		return sqlString.toString();

	}

	public static String setSfarsitDelegatie() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" update sapprd.zdelegatiehead set ora_sosire=?, distreal=? where id=? ");
		return sqlString.toString();
	}

	public static String updatePuncte() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" update sapprd.zdelegatieruta set vizitat = ? where id = ? and poz=? ");
		return sqlString.toString();

	}

	public static String getKmCota() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cotakm from sapprd.pa9001 where mandt='900' ");
		sqlString.append(" and pernr =? and to_date(begda,'yyyymmdd') <=to_date(?,'yyyymmdd') and to_date(endda,'yyyymmdd') >=to_date(?,'yyyymmdd') ");

		return sqlString.toString();
	}

	public static String getDelModifHeader() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select id, data_plecare, data_sosire from sapprd.zdelegatiehead h where h.codangajat =? ");
		sqlString.append(" and (not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = h.id and status in ('6')) ");
		sqlString.append(" and (not exists (select 1 from sapprd.zdelegturisme t where t.id = h.id and preluat='X')) ");
		sqlString.append(" ) order by id");
		return sqlString.toString();
	}

	public static String getDelModifStartStop() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select localitate||' / '||judet punct from sapprd.zdelegatieruta where id =? order by to_number(poz) ");

		return sqlString.toString();
	}

	public static String getDelegatieModif() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select nrauto, data_plecare, ora_plecare, data_sosire from sapprd.zdelegatiehead where id =? ");

		return sqlString.toString();
	}

	public static String existaCoordonate() {

		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select 1 from sapprd.zcoordlocalitati where mandt='900' and judet=? and localitate =? ");
		return sqlString.toString();

	}

	public static String adaugaCoordonate() {

		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" insert into sapprd.zcoordlocalitati (mandt, judet, localitate, latitudine, longitudine) values ('900',?,?,?,?) ");
		return sqlString.toString();

	}

	public static String getCoordonateOpriri() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select  lat, lon, km, gtime from nexus_gps_data where ");
		sqlString.append(" vcode in ");
		sqlString.append(" (select vcode from our_vehicles n, sapprd.zdelegatiehead z where ");
		sqlString.append(" trim(regexp_replace(n.car_number,'-| ','')) = trim(regexp_replace(z.nrauto,'-| ','')) and z.id =? )  ");
		sqlString.append(" and gtime BETWEEN to_date(?, 'dd-mm-yyyy hh24:mi:ss') and ");
		sqlString.append(" to_date(?, 'dd-mm-yyyy hh24:mi:ss')  and speed = 0 ");
		sqlString.append(" order by km ");

		return sqlString.toString();
	}

	public static String updateDistantaRecalculata() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" update sapprd.zdelegatiehead set  distrecalc=? where id=? ");
		return sqlString.toString();
	}

	public static String stergePuncteTraseu() {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" delete from sapprd.zdelegatieruta where  id =? and to_number(poz) > 1 and to_number(poz) < 100 ");

		return sqlString.toString();

	}

	public static String stergeLocalitatiAdaugate() {

		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" delete from sapprd.zdelegatieruta where  id =? and init ='0' ");

		return sqlString.toString();

	}

	public static String actualizeazaStareFaz() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" update sapprd.zdelegturisme set preluat = ' ' where id=? ");

		return sqlString.toString();

	}

	public static String getSubordVanzari(String unitLogQs, String departQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, nume from personal where (functie in ");
		sqlString.append(" (select cod from functii_non_vanzari where aprobat=?) or cod = ?) ");
		sqlString.append(" and tip <> '0' and filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" and substr(departament,0,2) in ");
		sqlString.append(departQs);
		sqlString.append(" order by nume");

		return sqlString.toString();
	}
	
	public static String getConsilieriIP() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, upper(nume) nume from personal where functie = ");
		sqlString.append(" 'CVIP' ");
		sqlString.append(" order by nume");

		return sqlString.toString();
	}

	public static String getSubordNonVanzari(String unitLogQs) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, nume from personal where (functie in ");
		sqlString.append(" (select cod from functii_non_vanzari where aprobat=?) or cod = ? )");
		sqlString.append(" and tip <> '0' and filiala in ");
		sqlString.append(unitLogQs);
		sqlString.append(" order by nume");

		return sqlString.toString();
	}

	public static String getSubordDirectorJuridic() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, nume from personal where (functie in ");
		sqlString.append(" (select cod from functii_non_vanzari where aprobat=?) or cod = ? )");
		sqlString.append(" and tip <> '0' ");
		sqlString.append(" order by nume");

		return sqlString.toString();
	}

	public static String getMasiniAngajatData() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct trim(regexp_replace(c.ktext,' ','')) ktext, ");
		sqlString.append(" case when a.adatu='19000101' then (SELECT TO_CHAR(SYSDATE, 'YYYYMMDD') FROM dual) ");
		sqlString.append(" else a.adatu end as adatu ");
		sqlString.append(" from sapprd.anlz a join sapprd.anla b on b.anln1 = a.anln1 and b.anln2 = a.anln2 and b.mandt=a.mandt ");
		sqlString.append(" join sapprd.aufk c on c.aufnr = a.caufn and c.mandt=a.mandt ");
		sqlString.append(" where a.pernr =? and c.auart='2001' and c.phas1='X' ");
		sqlString.append(" and a.bdatu >= ? and b.deakt = '00000000' and a.mandt='900' ");
		sqlString.append(" order by adatu desc ");

		return sqlString.toString();

	}

	public static String getMasiniAngajat() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct trim(regexp_replace(c.ktext,' ','')) ktext,a.adatu ");
		sqlString.append(" from sapprd.anlz a join sapprd.anla b on b.anln1 = a.anln1 and b.anln2 = a.anln2 and b.mandt=a.mandt ");
		sqlString.append(" join sapprd.aufk c on c.aufnr = a.caufn and c.mandt=a.mandt ");
		sqlString.append(" where a.pernr =? and c.auart='2001' ");
		sqlString.append(" and a.bdatu >= (select to_char(sysdate-15,'YYYYMMDD') from dual) and b.deakt = '00000000' and a.mandt='900' ");
		sqlString.append(" order by a.adatu desc ");

		return sqlString.toString();

	}

	public static String getCodAprobare() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select f.fid, f.aprobat from personal p, functii_non_vanzari f where p.cod =? and p.functie = f.cod ");

		return sqlString.toString();
	}

	public static String getIntervalDelegatie() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select to_char(to_date(data_plecare,'yyyymmdd')) plecare, ");
		sqlString.append(" to_char(to_date(data_sosire,'yyyymmdd')) sosire from sapprd.zdelegatiehead where mandt='900' and id=? ");

		return sqlString.toString();
	}

	public static String getMailDZFiliala() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select mail from personal where functie='DZ' and filiala = ");
		sqlString.append(" (select filiala from personal where cod=(select codangajat from sapprd.zdelegatiehead where mandt='900' and id=?)) ");

		return sqlString.toString();
	}

	public static String setDelegatieNeterminata() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" update sapprd.zdelegatiehead set distreal=-1 where id=? ");

		return sqlString.toString();
	}

	public static String getCodAprobareConsilieri() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala =(select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('SMG','SDCVA','DZ','SM') and p.functie = f.aprobat and f.cod=? order by aprobat desc  ");

		return sqlString.toString();
	}

	public static String getCodAprobareKA() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala =(select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('SDKA','DZ') and p.functie = f.aprobat and f.cod=? ");

		return sqlString.toString();
	}

	public static String getCodAprobareKA1() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala =(select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('SDKA','DZ') and p.functie = f.aprobat and f.cod=? ");
		sqlString.append(" union select  f.fid, f.aprobat from functii_non_vanzari f  where upper(f.cod) = 'KA1' and f.aprobat ='DZ'");

		return sqlString.toString();
	}

	public static String getCodAprobareAV() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select  f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala =(select filiala from personal where cod=?) ");
		sqlString.append(" and p.departament =(select departament from personal where cod=?) ");
		sqlString.append(" and p.functie = 'SD' and p.functie = f.aprobat and f.cod='AV' ");
		sqlString.append(" union ");
		sqlString.append(" select  f.fid, f.aprobat from functii_non_vanzari f  where f.cod='AV' and f.aprobat ='DZ'");

		return sqlString.toString();
	}

	public static String getCodAprobareCVW() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append("select  f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala =(select filiala from personal where cod=?) ");
		sqlString.append("and p.functie = 'SMW' and p.functie = f.aprobat and f.cod='CVW' ");
		sqlString.append("union ");
		sqlString.append("select  f.fid, f.aprobat from functii_non_vanzari f  where f.cod='CVW' and f.aprobat ='DZ' ");

		return sqlString.toString();
	}

	public static String getCodAprobareCVR() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala = (select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('SMR','DD') and p.functie = f.aprobat and f.cod=? ");

		return sqlString.toString();

	}

	public static String getCodAprobareCVIP() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala = (select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('SDIP','DZ') and p.functie = f.aprobat and f.cod=? ");

		return sqlString.toString();

	}
	
	public static String getCodAprobareCVA() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala = (select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('SMG','DZ') and p.functie = f.aprobat and f.cod=? ");

		return sqlString.toString();

	}

	public static String getCodAprobareRGEST() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct f.fid, f.aprobat from personal p, functii_non_vanzari f ");
		sqlString.append(" where p.filiala = (select filiala from personal where cod=?) ");
		sqlString.append(" and p.functie in ('DZ','DAG') and p.functie = f.aprobat and f.cod=? ");

		return sqlString.toString();

	}

	public static String getCodAprobareKA08() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select fid from functii_non_vanzari where cod='KA08' and aprobat = 'SD' ");

		return sqlString.toString();
	}

	public static String getCodAprobareKA05() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select fid from functii_non_vanzari where cod='KA05' and aprobat = 'SD' ");

		return sqlString.toString();
	}

	public static String getCoordonateOpriri(String codDisp) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select  lat, lon, km, vcode,gtime from nexus_gps_data where ");
		sqlString.append(" vcode in ");
		sqlString.append(codDisp);
		sqlString.append(" and trunc(gtime) between to_date(?, 'dd-mm-yyyy') and ");
		sqlString.append(" to_date(?, 'dd-mm-yyyy')  and speed = 0 ");
		sqlString.append(" order by km ");

		return sqlString.toString();
	}

	public static String getMasiniAlocateData() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct  n.vcode ");
		sqlString.append(" from our_vehicles n, sapprd.anlz a , sapprd.anla b,sapprd.aufk c where ");
		sqlString.append(" a.mandt='900' and b.mandt='900' and c.mandt='900' and b.anln1 = a.anln1 ");
		sqlString.append(" and b.anln2 = a.anln2 and b.mandt=a.mandt ");
		sqlString.append(" and c.aufnr = a.caufn and c.mandt=a.mandt ");
		sqlString.append(" and a.pernr =? ");
		sqlString.append(" and trim(regexp_replace(n.car_number,'-| ','')) = trim(regexp_replace(c.ktext,'-| ',''))  ");
		sqlString.append(" and a.bdatu >=? and b.deakt = '00000000' and a.mandt='900' ");

		return sqlString.toString();

	}

	public static String getNrAutoCodGps() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select ktext from sapprd.aufk where mandt='900' and vcode= ? ");
		return sqlString.toString();
	}

	public static String delegatiiSuprapuse() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select id from sapprd.zdelegatiehead where mandt='900' and codangajat =? ");
		sqlString.append(" and ( ");
		sqlString.append(" to_date(?,'yyyymmdd') between to_date(data_plecare,'yyyymmdd') and to_date(data_sosire,'yyyymmdd') ");
		sqlString.append(" or ");
		sqlString.append(" to_date(?,'yyyymmdd') between to_date(data_plecare,'yyyymmdd') and to_date(data_sosire,'yyyymmdd') ");
		sqlString.append(" or ");
		sqlString.append(" to_date(data_sosire,'yyyymmdd') between to_date(?,'yyyymmdd') and to_date(?,'yyyymmdd') ");
		sqlString.append(" ) ");
		sqlString.append(" and not exists( select 1 from sapprd.zdelstataprob b where b.iddelegatie = id and status = '6') ");

		return sqlString.toString();

	}

	public static String getCodAprobareDZ() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select fid from functii_non_vanzari  where cod ='WKND' and aprobat = 'DZ' ");

		return sqlString.toString();
	}

	public static String getCodAprobareJuridic() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select decode (p.filiala,'GL90',(select fid from functii_non_vanzari where cod=? and aprobat = 'DJ'), ");
		sqlString.append(" (select fid from functii_non_vanzari where cod=? and aprobat != 'DJ' )) fid, ");
		sqlString.append(" p.filiala from personal p where p.cod =? ");

		return sqlString.toString();
	}

	public static String getDelegatiiSfarsitLuna() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(
				" select id, codangajat, nrauto, data_plecare from sapprd.zdelegatiehead where data_plecare >=? and data_plecare <=? and data_sosire>? and distcalcluna = 0 ");

		return sqlString.toString();
	}

	public static String setKmSfarsitLuna() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append("update sapprd.zdelegatiehead set distcalcluna=? where id=? ");
		return sqlString.toString();

	}

	public static String getCodAprobareATRFiliale() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select fid from functii_non_vanzari where cod = 'ATR' and aprobat !='DAG' ");
		return sqlString.toString();
	}

	public static String getCodAprobareSBAFiliale() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select fid from functii_non_vanzari where cod = 'SBA' and aprobat ='DADMIN' ");
		return sqlString.toString();
	}

	public static String getCodAprobareSBAGLCentral() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select fid from functii_non_vanzari where cod = 'SBA' and aprobat ='SSLO' ");
		return sqlString.toString();
	}

	public static String getFunctiiConducere() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select distinct nume, filiala, functie, departament, mail from personal where ");
		sqlString.append(
				" functie in (select distinct aprobat from functii_non_vanzari where aprobat not in ('SMW','SMR') ) order by filiala, functie, departament ");

		return sqlString.toString();
	}

	public static String getCategoriiSubordonati() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, descriere from functii_non_vanzari where aprobat = ? and cod != 'WKND' ");
		sqlString.append(" union ");
		sqlString.append(" select cod, descriere from functii_non_vanzari where aprobat in ");
		sqlString.append(" (select cod from functii_non_vanzari where aprobat = ? ) and cod != 'WKND' ");

		return sqlString.toString();
	}

	public static String getAngajatiCategorieVanzari(String unitLogs, String tipuri) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, nume, functie from personal where filiala in ");
		sqlString.append(unitLogs);
		sqlString.append(" and functie in ");
		sqlString.append(tipuri);
		sqlString.append(" and departament =?  order by nume ");

		return sqlString.toString();
	}

	public static String getAngajatiCategorieNonVanzari(String unitLogs, String tipuri) {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cod, nume, functie from personal where filiala in ");
		sqlString.append(unitLogs);
		sqlString.append(" and functie in ");
		sqlString.append(tipuri);
		sqlString.append(" order by nume ");

		return sqlString.toString();
	}

	public static String getPozitieMasina() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select lat, lon, to_char(gtime,'dd Mon hh24:mi') data from nexus_gps_data where gtime = ");
		sqlString.append(" (select max(gtime) from nexus_gps_data where vcode=? ) ");
		sqlString.append("	and vcode=? ");

		return sqlString.toString();
	}

	public static String getNumeAngajat() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select nume from personal where cod = ?");

		return sqlString.toString();
	}

	public static String getAngajatiCuDelegatii() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select codangajat from sapprd.zdelegatiehead where ");
		sqlString.append(" to_date(?,'yyyymmdd') between to_date(data_plecare,'yyyymmdd') and to_date(data_sosire,'yyyymmdd') ");
		sqlString.append(" and not exists (select 1 from sapprd.zdelstataprob b where b.iddelegatie = id and status = '6') ");

		return sqlString.toString();
	}

	public static String getKmEfectuati() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select max(km) - min(km) from nexus_gps_data where trunc(gtime) = ? and vcode = ?  ");
		return sqlString.toString();
	}

	public static String getKmPrag() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select cast (cotakm as int) + cast (cotakm as int) * 0.5 km_prag from sapprd.pa9001 where mandt='900' ");
		sqlString.append(" and pernr =? and to_date(begda,'yyyymmdd') <=to_date(?,'yyyymmdd') ");
		sqlString.append(" and to_date(endda,'yyyymmdd') >=to_date(?,'yyyymmdd') ");

		return sqlString.toString();
	}

	public static String getDistante() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select vcode, max(km) - min(km)  from nexus_gps_data where trunc(gtime) = to_date(?,'dd-mm-yyyy') group by vcode ");
		sqlString.append(" having  max(km) - min(km) > 20   ");

		return sqlString.toString();
	}

	public static String getAngajatGps() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select distinct a.pernr ");
		sqlString.append(" from sapprd.anlz a join sapprd.anla b on b.anln1 = a.anln1 and b.anln2 = a.anln2 and b.mandt=a.mandt ");
		sqlString.append(" join sapprd.aufk c on c.aufnr = a.caufn and c.mandt=a.mandt ");
		sqlString.append(" where a.adatu<=? and a.bdatu >=? and b.deakt = '00000000' and a.mandt='900' ");
		sqlString.append(" and trim(regexp_replace(c.ktext,'-| ','')) in (select distinct trim(regexp_replace(n.car_number,'-| ','')) ");
		sqlString.append(" from websap.our_vehicles n where vcode =? ) ");

		return sqlString.toString();
	}

	public static String isAngajatLiberKm() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select liber  from sapprd.pa9001 where mandt='900'  ");
		sqlString.append(" and pernr =? and to_date(begda,'yyyymmdd') <=to_date(?,'yyyymmdd')  ");
		sqlString.append(" and to_date(endda,'yyyymmdd') >=to_date(?,'yyyymmdd')  ");

		return sqlString.toString();
	}

	public static String getAdresaMailAngajat() {
		StringBuilder sqlString = new StringBuilder();

		sqlString.append(" select mail from personal where cod = ? ");

		return sqlString.toString();
	}



	public static String getStareGps() {

		StringBuilder sqlString = new StringBuilder();
		sqlString.append(" select  to_char(gtime,'dd-Mon-yy HH24:mi:ss') datac, lat from nexus_gps_data d where ");
		sqlString.append(" d.vcode = (select vcode from our_vehicles where trim(regexp_replace(car_number,'-| ','')) = ?) ");
		sqlString.append(" and gtime = (select max(gtime) from nexus_gps_data where vcode = d.vcode) ");

		return sqlString.toString();
	}

	public static String getCodAprobareSD04() {
		StringBuilder sqlString = new StringBuilder();
		sqlString.append("select distinct v.fid from agenti a, functii_non_vanzari v where a.tip = 'SD' ");
		sqlString.append("and substr(a.divizie,0,2) = '04' and a.activ = 1 ");
		sqlString.append("and a.filiala = (select  filiala from agenti where cod =?) ");
		sqlString.append("and v.cod = 'AV' and v.aprobat = a.tip ");

		return sqlString.toString();
	}

}
