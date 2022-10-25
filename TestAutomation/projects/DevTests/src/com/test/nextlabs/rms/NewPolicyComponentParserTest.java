package com.test.nextlabs.rms;

import static org.junit.Assert.assertEquals;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.ENVS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;

import org.junit.Test;

import com.nextlabs.rms.services.manager.PolicyComponentParserV2;
import com.nextlabs.rms.services.manager.PolicyComponentParserV3;

public class NewPolicyComponentParserTest {

	/*
	 * String[] policyComponentString=new String[]{
	 * " FOR ((resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") AND resource.fso.dummy property1 = \"propertyBlah\" AND NOT (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\"))))"
	 * , //
	 * "FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp2 = \"property1\" AND pranava = \"pranava\" AND NOT ((resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")))"
	 * , //
	 * "FOR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")"
	 * , //
	 * "FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp2 = \"property1\" AND NOT ((resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")))"
	 * , //
	 * "FOR (((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") OR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")) AND NOT (resource.fso.comp2 = \"property1\"))"
	 * ,
	 * "FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\" AND NOT (((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND (resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\"))))"
	 * , //
	 * "FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND resource.fso.\"comp2property1\" = \"\" AND NOT ((resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")))"
	 * , //
	 * "FOR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")"
	 * , //
	 * "FOR (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")) AND (resource.fso.pranava=\"Pranava\" OR resource.fso.Pranava1=\"Pranava1\"))"
	 * , //
	 * "FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND resource.fso.\"comp2property1\" = \"\" AND NOT ((resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")))"
	 * , //
	 * "FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND (resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\"))"
	 * ,
	 * "FOR (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")) AND NOT (resource.fso.\"comp2property1\" = \"\"))"
	 * }; //String testString=
	 * "FOR ((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") AND resource.fso.comp2 = \"property1\" AND NOT ((resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")))"
	 * ; //String testString=
	 * "((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\"))"
	 * ; //String testString=
	 * "FOR ((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") AND resource.fso.\"comp2property1\" = \"\" AND NOT ((resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")))"
	 * ; //String testString=
	 * "FOR (((resource.fso.comp1 = \"property1\" AND resource.fso.comp1 = \"property2\") OR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")) AND NOT (resource.fso.comp2 = \"property1\"))"
	 * ; String testString=
	 * "FOR (((resource.fso.comp1property1 = \"property1\" AND resource.fso.comp1property2 = \"property2\") OR (resource.fso.comp3prop1 = \"property1\" AND resource.fso.comp3prop2 = \"property2\" AND resource.fso.comp3prop3 = \"property3\")) AND (resource.fso.pranava=\"Pranava\" OR resource.fso.Pranava1=\"Pranava1\"))"
	 * ; //String testString=
	 * "(((a1 = \"a1\" AND a2 = \"a2\") OR (a3 = \"a3\" AND a4  = \"a4\")) OR (a5 = \"a5\" AND a6 = \"a6\"))"
	 * ; //parser.parsePolicy(
	 * "FOR (((r1 = \"r1\" AND r2 = \"r2\") OR (r3 = \"r3\" AND r4 = \"r4\")) AND (r5 = \"r5\" AND r6 = \"r6\") OR ((r7 = \"r7\" & r8 = \"r8\") AND NOT (r9 = \"r9\")))"
	 * ); //parser.parsePolicy(testString); for(int
	 * i=0;i<policyComponentString.length-1;i++){
	 * System.out.println("Original String: ");
	 * System.out.println(policyComponentString[i]);
	 * System.out.println("Processed list");
	 * parser.parseResources(policyComponentString[i]);
	 * System.out.println("----------------------------\n"); }
	 */
//	 String testRes="FOR resource.fso.name = \"**.jt\"";
//	 String testSubject="BY (user.GROUP has 0 AND ((application.path = \"**\\acrord32.exe\" AND application.publisher = \"adobe systems, incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWRD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\EXCEL.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"SAP SE\")))";
//	 String testSubject="BY ((application.path = \"**\\acrord32.exe\" AND application.publisher = \"adobe systems, incorporated\" AND application.name = AdobeAcrobat) OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWRD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\EXCEL.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"SAP SE\"))";
//	String testSubject="BY (user.u1 = v1 OR ((application.n1 = V1 AND application.n2 > V2 AND application.n3 = V3 AND application.\"name with space\" != \"value with space\") OR (user.name = nameVal AND user.\"loc with space\" <= \"San Mateo\")) OR (host.h1 = hh AND host.h2 = \"with space\"))";//AND (host.h1 = hh AND host.h2 = \"with space\")(user.u1 = v1 OR 
//	String testSubject = "BY (user.u1 = v1 AND host.name = prince OR host.ip = 11111 OR (application.name = Microsoft AND application.url = \"http://localhost:8443/RMS\"))" ;
//	String testSubject = "BY user.Group has 0 OR (host.name = prince AND host.param = value) AND (application.name = ACROBAT AND application.path = \\*Drive\\*)";
//	String testSubject = "BY user.Group has 0 OR host.name = prince AND application.name = ACROBAT OR application.path = \\*Drive\\*";
//	String testSubject = "BY user.Group has 0";
//	String testSubject = "BY TRUE";
//	String testRes="FOR (resource.fso.name1 = val1 OR (resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"sudhi\") OR (resource.fso.name = \"**\" AND resource.fso.name = \"pranava\"))";
//	String testRes = "FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 OR resource.fso.name3 = val3 OR resource.fso.name4 = val4";
//	String testRes = "FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 AND resource.fso.name3 = val3 AND resource.fso.name4 = val4";
//	 String
//	 testRes="FOR ((resource.fso.dummy property2 = \"property2\" AND resource.fso.name = \"**\" AND resource.fso.name = \"pranava\") AND NOT ((resource.fso.name = \"**\" OR resource.fso.name = \"**\")) )";
//	String testRes = "FOR TRUE";
	// String testSubject="BY user.GROUP has 3";
//	String testSubject = "BY user.groups = \"Support Analyst\"";
	// String testSubject = "BY NOT (application.path = \"**\\excel.exe\")";
	
	POLICY pol;
	RESOURCES res;
	USERS users;
	APPLICATIONS apps;
	LOCATIONS locs;
	PolicyComponentParserV3 parser;
	ENVS envs;

    @Test
    public void testResource() {
        String[] resBundleStrArray = new String[] {
                "FOR TRUE",
                "FOR resource.fso.name1 = val1",
                "FOR resource.fso.name1 = val1 AND resource.fso.name2 = val2",
                "FOR (resource.fso.name1 = val1 AND (resource.fso.name2 = val2 AND resource.fso.name3 = val3))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND (resource.fso.name3 = val3 AND resource.fso.name4 = val4))",
                "FOR (resource.fso.name1 = val1 AND ((resource.fso.name2 = val2 AND resource.fso.name3 = val3) AND (resource.fso.name4 = val4 AND resource.fso.name5 = val5)))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND ((resource.fso.name3 = val3 AND resource.fso.name4 = val4) AND (resource.fso.name5 = val5 AND resource.fso.name6 = val6)))",
                "FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2",
                "FOR (resource.fso.name1 = val1 OR resource.fso.name2 = val2 OR (resource.fso.name3 = val3))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) OR (resource.fso.name3 = val3 AND resource.fso.name4 = val4))",
                "FOR (resource.fso.name1 = val1 OR ((resource.fso.name2 = val2 AND resource.fso.name3 = val3) AND (resource.fso.name4 = val4 AND resource.fso.name4 = val4)))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) OR ((resource.fso.name3 = val3 AND resource.fso.name4 = val4) AND (resource.fso.name5 = val5 AND resource.fso.name6 = val6)))",
                "FOR NOT(resource.fso.name1 = val1)",
                "FOR NOT(resource.fso.name1 = val1 AND resource.fso.name2 = val2)",
                "FOR ((resource.fso.name1 = val1) AND NOT((resource.fso.name2 = val2 AND resource.fso.name3 = val3)))",
                "FOR (resource.fso.name1 = val1 OR ((resource.fso.name2 = val2) AND NOT((resource.fso.name3 = val3 AND resource.fso.name3 = val3))))",
                "FOR (resource.fso.name1 = val1 AND ((resource.fso.name2 = val2) AND NOT((resource.fso.name3 = val3 AND resource.fso.name4 = val4))))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) OR ((resource.fso.name3 = val3) AND NOT((resource.fso.name4 = val4 AND resource.fso.name5 = val5))))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND ((resource.fso.name3 = val3) AND NOT((resource.fso.name4 = val4 AND resource.fso.name5 = val5))))",
                // these last 2 test cases will fail during the call to processPolicy(), and this is a limitation of the code
                "FOR (NOT(resource.fso.name1 = val1 AND NOT ((resource.fso.name2 = val2)) AND resource.fso.name3 = val3))",
                "FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND NOT (((resource.fso.name3 = val3) AND NOT((resource.fso.name4 = val4 AND resource.fso.name5 = val5))))"
        };

        String[] polOutput = new String[] {
                "<xml-fragment/>",
                "<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
                "<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
                "<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"false\" type=\"res\">0</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"false\" type=\"res\">0</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">2</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">2</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">0</CONDITION>\r\n</xml-fragment>",
                "<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
                "<CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION>",
                "<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"false\" type=\"res\">0,2</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">2</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">0</CONDITION>\r\n</xml-fragment>",
                "<CONDITION exclude=\"true\" type=\"res\">0</CONDITION>",
                "<CONDITION exclude=\"true\" type=\"res\">0</CONDITION>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"true\" type=\"res\">0</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"true\" type=\"res\">0</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">1,2</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"true\" type=\"res\">0</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">1</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">2</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"true\" type=\"res\">1</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">0</CONDITION>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <CONDITION exclude=\"true\" type=\"res\">1</CONDITION>\r\n  <CONDITION exclude=\"false\" type=\"res\">0</CONDITION>\r\n</xml-fragment>"
        };

        String[] resOutput = new String[] {
                "<xml-fragment/>",
                "<RESOURCE id=\"0\">\r\n  <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n</RESOURCE>",
                "<RESOURCE id=\"0\">\r\n  <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n</RESOURCE>",
                "<RESOURCE id=\"0\">\r\n  <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n</RESOURCE>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name6\" value=\"val6\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name6\" value=\"val6\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<RESOURCE id=\"0\">\r\n  <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n</RESOURCE>",
                "<RESOURCE id=\"0\">\r\n  <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n</RESOURCE>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"2\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>",
                "<xml-fragment>\r\n  <RESOURCE id=\"1\">\r\n    <PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n  <RESOURCE id=\"0\">\r\n    <PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/>\r\n    <PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/>\r\n  </RESOURCE>\r\n</xml-fragment>"
        };

        for (int i=0; i < resBundleStrArray.length; ++i) {
            pol = POLICY.Factory.newInstance();
            res = RESOURCES.Factory.newInstance();
            users = USERS.Factory.newInstance();
            apps = APPLICATIONS.Factory.newInstance();
            locs = LOCATIONS.Factory.newInstance();
            envs = ENVS.Factory.newInstance();
            parser = new PolicyComponentParserV3(res, users, apps, locs, envs);

            parser.processPolicy(pol, resBundleStrArray[i], "BY TRUE", "WHERE TRUE");

            assertEquals(pol.toString(), polOutput[i]);
            assertEquals(res.toString(), resOutput[i]);
        }
    }

	@Test
	public void testSubject() {
		String[] testSubject = new String[]{"BY (user.GROUP has 0 AND ((application.prop = \"prop\" AND application.pro = \"pro\") AND application.blah1 = \"blah1\" AND (application.prop1 = \"prop1\" AND application.prop2 = \"prop2\") AND ((application.prop3 = \"2.45AM\" AND application.prop4 = \"2.45 AM\") AND application.blah = \"blah\")))",
				"BY ((user.locationcode = \"SG\" AND user.user_location_code = \"IN\") AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\") AND (user.GROUP has 2 AND user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\"))",
				"BY (user.u1 = v1 OR ((application.n1 = V1 AND application.n2 > V2 AND application.n3 = V3 AND application.\"name with space\" != \"value with space\") OR (user.name = nameVal AND user.\"loc with space\" <= \"San Mateo\")) OR (host.h1 = hh AND host.h2 = \"with space\"))",
				"BY user.Group has 0 OR host.name = prince AND application.name = ACROBAT OR application.path = Drive",
				"BY user.Group has 0",
				"BY TRUE",
				"BY TRUE",
				"BY TRUE",
				"BY TRUE",
				"BY TRUE",
				"BY TRUE"
				};
		
		String[] testRes = new String[] {"FOR TRUE",
				"FOR TRUE",
				"FOR TRUE",
				"FOR TRUE",
				"FOR TRUE",
				"FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 OR resource.fso.name3 = val3 OR resource.fso.name4 = val4",
				"FOR resource.fso.name = \"**.jt\"",
				"FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 AND resource.fso.name3 = val3 AND resource.fso.name4 = val4",
				"FOR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")",
				"FOR ((resource.fso.comp3 = \"property1\" OR resource.fso.comp3 = \"property2\") AND (resource.fso.comp3 = \"property3\" AND resource.fso.comp4 = \"property4\"))",
				"FOR (resource.fso.program = \"PR-01\" AND resource.fso.bafa = \"BAFA-01\")"
				};
		
		String[] testEnv = new String[] {"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				""
				};
		
		String[] envsOutput = new String[] {"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
				};

		String[] policyOutput = new String[]{"<xml-fragment usergroup=\"0\"><CONDITION exclude=\"false\" type=\"app\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">1</CONDITION><CONDITION exclude=\"false\" type=\"app\">2</CONDITION></xml-fragment>",
		"<xml-fragment usergroup=\"2\"><CONDITION exclude=\"false\" type=\"usr\">0</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION><CONDITION exclude=\"false\" type=\"usr\">1</CONDITION></xml-fragment>",
		"<xml-fragment><CONDITION exclude=\"false\" type=\"app\">0</CONDITION><CONDITION exclude=\"false\" type=\"usr\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION></xml-fragment>",
		"<xml-fragment usergroup=\"0\"><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1</CONDITION></xml-fragment>",
		"<xml-fragment usergroup=\"0\"/>",
		"<CONDITION exclude=\"false\" type=\"res\">0,1,2,3</CONDITION>",
		"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
		"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
		"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
		"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION></xml-fragment>",
		"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>"
		};

		String[] appsOutput = new String[]{"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"prop\" value=\"prop\" method=\"EQ\"/><PROPERTY name=\"pro\" value=\"pro\" method=\"EQ\"/><PROPERTY name=\"blah1\" value=\"blah1\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"prop1\" value=\"prop1\" method=\"EQ\"/><PROPERTY name=\"prop2\" value=\"prop2\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"prop3\" value=\"2.45AM\" method=\"EQ\"/><PROPERTY name=\"prop4\" value=\"2.45 AM\" method=\"EQ\"/><PROPERTY name=\"blah\" value=\"blah\" method=\"EQ\"/></APPLICATION></xml-fragment>",
		"<xml-fragment/>",
		"<APPLICATION id=\"0\"><PROPERTY name=\"n1\" value=\"V1\" method=\"EQ\"/><PROPERTY name=\"n2\" value=\"V2\" method=\"GT\"/><PROPERTY name=\"n3\" value=\"V3\" method=\"EQ\"/><PROPERTY name=\"name with space\" value=\"value with space\" method=\"NE\"/></APPLICATION>",
		"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"name\" value=\"ACROBAT\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"Drive\" method=\"EQ\"/></APPLICATION></xml-fragment>",
		"<xml-fragment/>",
		"<xml-fragment/>",
		"<xml-fragment/>",
		"<xml-fragment/>",
		"<xml-fragment/>",
		"<xml-fragment/>",
		"<xml-fragment/>"
		};
		
		String[] usersOutput = new String[]{"<xml-fragment/>",
				"<xml-fragment><USER id=\"0\"><PROPERTY name=\"locationcode\" value=\"SG\" method=\"EQ\"/><PROPERTY name=\"user_location_code\" value=\"IN\" method=\"EQ\"/></USER><USER id=\"1\"><PROPERTY name=\"dummyproperty1\" value=\"dummy\" method=\"EQ\"/><PROPERTY name=\"dummyproperty2\" value=\"dummy2\" method=\"EQ\"/></USER></xml-fragment>",
				"<xml-fragment><USER id=\"0\"><PROPERTY name=\"name\" value=\"nameVal\" method=\"EQ\"/><PROPERTY name=\"loc with space\" value=\"San Mateo\" method=\"LE\"/></USER><USER id=\"1\"><PROPERTY name=\"u1\" value=\"v1\" method=\"EQ\"/></USER></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
				};
		
		
		String[] locsOutput = new String[]{"<xml-fragment/>",
				"<LOCATION id=\"0\"><PROPERTY name=\"inet_address\" value=\"10.63.1.144\" method=\"EQ\"/><PROPERTY name=\"inet_address\" value=\"10.63.0.101\" method=\"EQ\"/></LOCATION>",
				"<LOCATION id=\"0\"><PROPERTY name=\"h1\" value=\"hh\" method=\"EQ\"/><PROPERTY name=\"h2\" value=\"with space\" method=\"EQ\"/></LOCATION>",
				"<LOCATION id=\"0\"><PROPERTY name=\"name\" value=\"prince\" method=\"EQ\"/></LOCATION>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
				};
		
		String[] resOutput = new String[] {"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name\" value=\"**.jt\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"comp3\" value=\"property1\" method=\"EQ\"/><PROPERTY name=\"comp3\" value=\"property2\" method=\"EQ\"/><PROPERTY name=\"comp3\" value=\"property3\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"comp3\" value=\"property1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"comp3\" value=\"property2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"comp3\" value=\"property3\" method=\"EQ\"/><PROPERTY name=\"comp4\" value=\"property4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"EQ\"/><PROPERTY name=\"bafa\" value=\"BAFA-01\" method=\"EQ\"/></RESOURCE>"

		};
		
		for(int a = 0; a < testSubject.length; a++){
			System.out.println("Running testcase number "+a);
			pol = POLICY.Factory.newInstance();
			res = RESOURCES.Factory.newInstance();
			users = USERS.Factory.newInstance();
			apps = APPLICATIONS.Factory.newInstance();
			locs = LOCATIONS.Factory.newInstance();
			envs = ENVS.Factory.newInstance();
			parser = new PolicyComponentParserV3(res, users, apps, locs, envs);
			parser.processPolicy(pol, testRes[a], testSubject[a], testEnv[a]);
			System.out.println("Policy: \n"+pol.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("APPLICATION:\n"+apps.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("USER:\n"+users.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("LOCATION:\n"+locs.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("RESOURCE:\n"+res.toString().replaceAll("\\r\\n\\s*", ""));
			assertEquals(policyOutput[a], pol.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("Policy passed ");
			assertEquals(appsOutput[a], apps.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("Apps passed");
			assertEquals(usersOutput[a], users.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("Users passed");
			assertEquals(locsOutput[a], locs.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("Locations passed");
			assertEquals(envsOutput[a], envs.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("Envs passed");
			assertEquals(resOutput[a], res.toString().replaceAll("\\r\\n\\s*", ""));
			System.out.println("Resources passed");
		}
	}
	
	/*@Test
	public void testApp1() {
		pol = POLICY.Factory.newInstance();
		res = RESOURCES.Factory.newInstance();
		users = USERS.Factory.newInstance();
		apps = APPLICATIONS.Factory.newInstance();
		locs = LOCATIONS.Factory.newInstance();
		envs = ENVS.Factory.newInstance();
		parser = new PolicyComponentParserV3(res, users, apps, locs, envs);
		
		String resStr = "FOR TRUE";
		String subStr = "BY application.standard = \"ISO\" AND application.price >= 3000";
		String envStr = "";
		System.out.println("Processing policy");
		parser.processPolicy(pol, resStr, subStr, envStr);
		System.out.println("Processed policy");
		System.out.println("Policy: "+ pol.toString());
		System.out.println("Users: "+ users.toString());
		System.out.println("Applications: "+ apps.toString());
		System.out.println("Locations: "+locs.toString());
		System.out.println("Envs: "+envs.toString());
		String expectedPolicy = "<CONDITION exclude=\"false\" type=\"app\">0</CONDITION>";
		Users: <xml-fragment/>
		Applications: <APPLICATION id="0">
		  <PROPERTY name="standard" value="ISO" method="EQ"/>
		  <PROPERTY name="price" value="3000" method="GE"/>
		</APPLICATION>
		Locations: <xml-fragment/>
		Envs: <xml-fragment/>
	}*/

}
