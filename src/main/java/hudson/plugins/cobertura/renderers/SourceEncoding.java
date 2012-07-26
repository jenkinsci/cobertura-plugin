package hudson.plugins.cobertura.renderers;
/**
 * Encoding list which JDK provides
 * @author JunHo Yoon
 */
public enum SourceEncoding {
	ASCII("ASCII") ,
	Big5("Big5") ,
	Big5_HKSCS("Big5_HKSCS") ,
	Big5_Solaris("Big5_Solaris") ,
	Cp037("Cp037") ,
	Cp1006("Cp1006") ,
	Cp1025("Cp1025") ,
	Cp1026("Cp1026") ,
	Cp1046("Cp1046") ,
	Cp1047("Cp1047") ,
	Cp1097("Cp1097") ,
	Cp1098("Cp1098") ,
	Cp1112("Cp1112") ,
	Cp1122("Cp1122") ,
	Cp1123("Cp1123") ,
	Cp1124("Cp1124") ,
	Cp1140("Cp1140") ,
	Cp1141("Cp1141") ,
	Cp1142("Cp1142") ,
	Cp1143("Cp1143") ,
	Cp1144("Cp1144") ,
	Cp1145("Cp1145") ,
	Cp1146("Cp1146") ,
	Cp1147("Cp1147") ,
	Cp1148("Cp1148") ,
	Cp1149("Cp1149") ,
	Cp1250("Cp1250") ,
	Cp1251("Cp1251") ,
	Cp1252("Cp1252") ,
	Cp1253("Cp1253") ,
	Cp1254("Cp1254") ,
	Cp1255("Cp1255") ,
	Cp1256("Cp1256") ,
	Cp1257("Cp1257") ,
	Cp1258("Cp1258") ,
	Cp1381("Cp1381") ,
	Cp1383("Cp1383") ,
	Cp273("Cp273") ,
	Cp277("Cp277") ,
	Cp278("Cp278") ,
	Cp280("Cp280") ,
	Cp284("Cp284") ,
	Cp285("Cp285") ,
	Cp297("Cp297") ,
	Cp33722("Cp33722") ,
	Cp420("Cp420") ,
	Cp424("Cp424") ,
	Cp437("Cp437") ,
	Cp500("Cp500") ,
	Cp737("Cp737") ,
	Cp775("Cp775") ,
	Cp838("Cp838") ,
	Cp850("Cp850") ,
	Cp852("Cp852") ,
	Cp855("Cp855") ,
	Cp856("Cp856") ,
	Cp857("Cp857") ,
	Cp858("Cp858") ,
	Cp860("Cp860") ,
	Cp861("Cp861") ,
	Cp862("Cp862") ,
	Cp863("Cp863") ,
	Cp864("Cp864") ,
	Cp865("Cp865") ,
	Cp866("Cp866") ,
	Cp868("Cp868") ,
	Cp869("Cp869") ,
	Cp870("Cp870") ,
	Cp871("Cp871") ,
	Cp874("Cp874") ,
	Cp875("Cp875") ,
	Cp918("Cp918") ,
	Cp921("Cp921") ,
	Cp922("Cp922") ,
	Cp930("Cp930") ,
	Cp933("Cp933") ,
	Cp935("Cp935") ,
	Cp937("Cp937") ,
	Cp939("Cp939") ,
	Cp942("Cp942") ,
	Cp942C("Cp942C") ,
	Cp943("Cp943") ,
	Cp943C("Cp943C") ,
	Cp948("Cp948") ,
	Cp949("Cp949") ,
	Cp949C("Cp949C") ,
	Cp950("Cp950") ,
	Cp964("Cp964") ,
	Cp970("Cp970") ,
	EUC_CN("EUC_CN") ,
	EUC_JP("EUC_JP") ,
	EUC_JP_LINUX("EUC_JP_LINUX") ,
	EUC_JP_Solaris("EUC_JP_Solaris") ,
	EUC_KR("EUC_KR") ,
	EUC_TW("EUC_TW") ,
	GB18030("GB18030") ,
	GBK("GBK") ,
	ISCII91("ISCII91") ,
	ISO2022_CN_CNS("ISO2022_CN_CNS") ,
	ISO2022_CN_GB("ISO2022_CN_GB") ,
	ISO2022CN("ISO2022CN") ,
	ISO2022JP("ISO2022JP") ,
	ISO2022KR("ISO2022KR") ,
	ISO8859_1("ISO8859_1") ,
	ISO8859_13("ISO8859_13") ,
	ISO8859_15("ISO8859_15") ,
	ISO8859_2("ISO8859_2") ,
	ISO8859_3("ISO8859_3") ,
	ISO8859_4("ISO8859_4") ,
	ISO8859_5("ISO8859_5") ,
	ISO8859_6("ISO8859_6") ,
	ISO8859_7("ISO8859_7") ,
	ISO8859_8("ISO8859_8") ,
	ISO8859_9("ISO8859_9") ,
	JISAutoDetect("JISAutoDetect") ,
	KOI8_R("KOI8_R") ,
	MacArabic("MacArabic") ,
	MacCentralEurope("MacCentralEurope") ,
	MacCroatian("MacCroatian") ,
	MacCyrillic("MacCyrillic") ,
	MacDingbat("MacDingbat") ,
	MacGreek("MacGreek") ,
	MacHebrew("MacHebrew") ,
	MacIceland("MacIceland") ,
	MacRoman("MacRoman") ,
	MacRomania("MacRomania") ,
	MacSymbol("MacSymbol") ,
	MacThai("MacThai") ,
	MacTurkish("MacTurkish") ,
	MacUkraine("MacUkraine") ,
	MS874("MS874") ,
	MS932("MS932") ,
	MS936("MS936") ,
	MS949("MS949") ,
	MS950("MS950") ,
	MS950_HKSCS("MS950_HKSCS") ,
	PCK("PCK") ,
	SJIS("SJIS") ,
	TIS620("TIS620") ,
	UnicodeBig("UnicodeBig") ,
	UnicodeBigUnmarked("UnicodeBigUnmarked") ,
	UnicodeLittle("UnicodeLittle") ,
	UnicodeLittleUnmarked("UnicodeLittleUnmarked") ,
	UTF_16("UTF-16") ,
	UTF_8("UTF-8") ,
	x_iso_8859_11("x-iso-8859-11") ,
	x_Johab("x-Johab");
	
	/**
	 * real encoding name
	 */
	private final String encodingName;

	/**
	 * Constructor
	 * @param encodingName real encoding name
	 */
	private SourceEncoding(String encodingName) {
		this.encodingName = encodingName;
	}

	/**
	 * Get real encoding name 
	 * @return encoding name
	 */
	public String getEncodingName() {
		return encodingName;
	}

	public static SourceEncoding getEncoding(String encodingName) {
		encodingName = encodingName.replace("-", "").toLowerCase();
		for (SourceEncoding encoding : values()) {
			if (encoding.getEncodingName().replace("-", "").toLowerCase().equals(encodingName)) {
				return encoding;
			}
		}
		return SourceEncoding.ASCII;
	}
}
