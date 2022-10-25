package com.test.nextlabs.rms;

import java.io.IOException;
import java.io.StringWriter;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.ENVS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;

import org.apache.xmlbeans.XmlOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nextlabs.rms.eval.UnsupportedComponentException;
import com.nextlabs.rms.services.manager.PolicyComponentParserV4;

public class NewPolicyComponentParserTest4 {
	private XmlOptions options;
	private boolean debug;
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void init() {
		options = new XmlOptions();
		debug = Boolean.valueOf(System.getProperty("test.debug"));
	}

	@Test
	public void testEnvironment() throws IOException {
		// @formatter:off
		String[][] data = { 
				{ 
					"(A AND B AND C AND D AND E AND (F OR G OR H OR I))",
					"BY TRUE",
					"FOR TRUE",
					"WHERE (ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 180 AND CURRENT_TIME.identity >= \"Jun 25, 2015 6:38:00 PM\" AND CURRENT_TIME.time >= \"6:38:00 PM\" AND CURRENT_TIME.time <= \"7:38:00 PM\" AND CURRENT_TIME.identity <= \"Jul 31, 2015 6:38:42 PM\" AND (CURRENT_TIME.weekday = \"sunday\" OR CURRENT_TIME.weekday = \"tuesday\" OR CURRENT_TIME.weekday = \"thursday\" OR CURRENT_TIME.weekday = \"saturday\"))",
					//result
					"<xml-fragment><CONDITION exclude=\"false\" type=\"env\">0</CONDITION><CONDITION exclude=\"false\" type=\"env\">1,2,3,4</CONDITION></xml-fragment>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment><ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" value=\"180\" method=\"GT\"/><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jun 25, 2015 6:38:00 PM\" method=\"GE\"/><PROPERTY name=\"CURRENT_TIME.time\" value=\"6:38:00 PM\" method=\"GE\"/><PROPERTY name=\"CURRENT_TIME.time\" value=\"7:38:00 PM\" method=\"LE\"/><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jul 31, 2015 6:38:42 PM\" method=\"LE\"/></ENV><ENV id=\"1\"><PROPERTY name=\"CURRENT_TIME.weekday\" value=\"sunday\" method=\"EQ\"/></ENV><ENV id=\"2\"><PROPERTY name=\"CURRENT_TIME.weekday\" value=\"tuesday\" method=\"EQ\"/></ENV><ENV id=\"3\"><PROPERTY name=\"CURRENT_TIME.weekday\" value=\"thursday\" method=\"EQ\"/></ENV><ENV id=\"4\"><PROPERTY name=\"CURRENT_TIME.weekday\" value=\"saturday\" method=\"EQ\"/></ENV></xml-fragment>"
				}
		};
		// @formatter:on
		XmlOptions options = new XmlOptions();
		for (int i = 0; i < data.length; i++) {
			StringWriter out = new StringWriter();
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV4 parser = new PolicyComponentParserV4(res, users, apps, locs, envs);
			parser.processPolicy(pol, data[i][1], data[i][2], data[i][3]);
			System.out.println("Processing : " + data[i][0]);
			if (debug) {
				System.out.println("Policy: \n" + pol.toString());
				System.out.println();
				System.out.println("Resources: \n" + res.toString());
				System.out.println();
				System.out.println("Users: \n" + users.toString());
				System.out.println();
				System.out.println("Applications: \n" + apps.toString());
				System.out.println();
				System.out.println("Locations: \n" + locs.toString());
				System.out.println();
				System.out.println("Environments: \n" + envs.toString());
				System.out.println();
			}
			pol.save(out, options);
			Assert.assertEquals("Invalid policy format for row: " + (i + 1), data[i][4], out.toString());
			out = new StringWriter();
			res.save(out, options);
			Assert.assertEquals("Invalid resource format for row: " + (i + 1), data[i][5], out.toString());
			out = new StringWriter();
			users.save(out, options);
			Assert.assertEquals("Invalid users format for row: " + (i + 1), data[i][6], out.toString());
			out = new StringWriter();
			apps.save(out, options);
			Assert.assertEquals("Invalid application format for row: " + (i + 1), data[i][7], out.toString());
			out = new StringWriter();
			locs.save(out, options);
			Assert.assertEquals("Invalid location format for row: " + (i + 1), data[i][8], out.toString());
			out = new StringWriter();
			envs.save(out, options);
			Assert.assertEquals("Invalid environment format for row: " + (i + 1), data[i][9], out.toString());
		}
	}

	@Test
	public void testMixComponent() throws IOException {
		// @formatter:off
			String[][] data = { 
					{ 
						"(issue 30328)",
						"BY (user.GROUP has 25 AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR (resource.fso.sensitivity = \"Non Business\" AND NOT ((resource.fso.program = \"PR-01\" OR resource.fso.bafa = \"BAFA-01\" OR resource.fso.ear = \"EAR-01\")))",
						"WHERE (TRUE AND ((CURRENT_TIME.identity >= \"Jun 23, 2015 5:27:30 PM\" AND ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 600) AND TRUE))",
						//result
						"<xml-fragment usergroup=\"25\"><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION><CONDITION exclude=\"false\" type=\"res\">3</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2</CONDITION><CONDITION exclude=\"false\" type=\"env\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"bafa\" value=\"BAFA-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ear\" value=\"EAR-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\excel.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\WINWORD.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<ENV id=\"0\"><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jun 23, 2015 5:27:30 PM\" method=\"GE\"/><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" value=\"600\" method=\"GT\"/></ENV>"
					},
					{ 
						"(issue 30328)",
						"BY ((host.GROUP has 4 OR host.inet_address = \"10.23.57.135\") AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\") AND NOT (resource.fso.program = \"PR-01\") AND (resource.fso.ear = \"EAR-01\" OR resource.fso.bafa = \"BAFA-02\"))",
						"WHERE (TRUE AND ((CURRENT_TIME.identity >= \"Jun 23, 2015 5:27:30 PM\" AND ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 600) AND TRUE))",
						//result
						"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION><CONDITION exclude=\"false\" type=\"res\">3,4</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2</CONDITION><CONDITION exclude=\"false\" type=\"env\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" value=\"Confidential\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"ear\" value=\"EAR-01\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"4\"><PROPERTY name=\"bafa\" value=\"BAFA-02\" method=\"EQ\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\excel.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\WINWORD.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment><LOCATION id=\"0\"><PROPERTY name=\"GROUP\" value=\"4\" method=\"has\"/></LOCATION><LOCATION id=\"1\"><PROPERTY name=\"inet_address\" value=\"10.23.57.135\" method=\"EQ\"/></LOCATION></xml-fragment>",
						"<ENV id=\"0\"><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jun 23, 2015 5:27:30 PM\" method=\"GE\"/><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" value=\"600\" method=\"GT\"/></ENV>"
					},
					{ 
						"(issue 30362)",
						"BY ((host.GROUP has 5 AND host.inet_address = \"10.23.56.55\") AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\" OR resource.fso.bafa = \"BAFA-02\") AND (resource.fso.type = \"doc.nxl\" OR resource.fso.type = \"docx.nxl\" OR resource.fso.type = \"xls.nxl\"))",
						"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 180 AND CURRENT_TIME.identity >= \"Jun 21, 2015 5:27:30 PM\") AND TRUE))",
						//result
						"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION><CONDITION exclude=\"false\" type=\"res\">3,4,5</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2</CONDITION><CONDITION exclude=\"false\" type=\"env\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" value=\"Confidential\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"bafa\" value=\"BAFA-02\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"type\" value=\"doc.nxl\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"4\"><PROPERTY name=\"type\" value=\"docx.nxl\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"5\"><PROPERTY name=\"type\" value=\"xls.nxl\" method=\"EQ\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\excel.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\WINWORD.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<LOCATION id=\"0\"><PROPERTY name=\"GROUP\" value=\"5\" method=\"has\"/><PROPERTY name=\"inet_address\" value=\"10.23.56.55\" method=\"EQ\"/></LOCATION>",
						"<ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" value=\"180\" method=\"GT\"/><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jun 21, 2015 5:27:30 PM\" method=\"GE\"/></ENV>"
					},
					{ 
						"(issue 30414)",
						"BY (user.GROUP has 21 AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"General Business\") AND NOT ((resource.fso.program = \"PR-01\" OR resource.fso.ear = \"EAR-01\")))",
						"",
						//result
						"<xml-fragment usergroup=\"21\"><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION><CONDITION exclude=\"false\" type=\"res\">3</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" value=\"General Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"ear\" value=\"EAR-01\" method=\"NE\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\excel.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\WINWORD.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{ 
						"(issue 30416)",
						"BY ((user.givenname = \"a\" AND user.telephonenumber = \"64292110\") AND NOT (application.path = \"**\\acrord32.exe\"))",
						"FOR resource.fso.department = \"Executive Management\"",
						"",
						//result
						"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"usr\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0</CONDITION></xml-fragment>",
						"<RESOURCE id=\"0\"><PROPERTY name=\"department\" value=\"Executive Management\" method=\"EQ\"/></RESOURCE>",
						"<USER id=\"0\"><PROPERTY name=\"givenname\" value=\"a\" method=\"EQ\"/><PROPERTY name=\"telephonenumber\" value=\"64292110\" method=\"EQ\"/></USER>",
						"<APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\acrord32.exe\" method=\"NE\"/></APPLICATION>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{ 
						"(issue 30731)",
						"BY (user.did = 10325 OR user.did = 37722 OR user.did = 37724) AND ((application.path = \"**\\Excel.exe\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\AcroRd32.exe\" AND application.publisher = \"Adobe Systems, Incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"sap ag\"))",
						"FOR (resource.fso.course = \"2300 Conductor Etch Process\" AND resource.fso.year = \"2014\") AND (resource.fso.path = \"**.ppt**\" OR resource.fso.path = \"**.xls**\" OR resource.fso.path = \"**.doc**\")",
						"",
						//result
						"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1,2,3</CONDITION><CONDITION exclude=\"false\" type=\"usr\">0,1,2</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2,3</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"course\" value=\"2300 Conductor Etch Process\" method=\"EQ\"/><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"path\" value=\"**.ppt**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"path\" value=\"**.xls**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"path\" value=\"**.doc**\" method=\"EQ\"/></RESOURCE></xml-fragment>",
						"<xml-fragment><USER id=\"0\"><PROPERTY name=\"did\" value=\"10325\" method=\"EQ\"/></USER><USER id=\"1\"><PROPERTY name=\"did\" value=\"37722\" method=\"EQ\"/></USER><USER id=\"2\"><PROPERTY name=\"did\" value=\"37724\" method=\"EQ\"/></USER></xml-fragment>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\Excel.exe\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Adobe Systems, Incorporated\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\POWERPNT.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"3\"><PROPERTY name=\"path\" value=\"**\\VEViewer.exe\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"sap ag\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{ 
						"(issue 30406)",
						"BY (user.GROUP has 13 AND host.GROUP has 4 AND application.path = \"**\\Excel.exe\")",
						"FOR ((resource.fso.program = \"PR-03\" OR resource.fso.bafa = \"BAFA-01\") AND NOT (resource.fso.type = \"pdf.nxl\"))",
						"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND CURRENT_TIME.time >= \"11:03:00 PM\" AND CURRENT_TIME.time <= \"12:03:16 AM\" AND CURRENT_TIME.weekday = \"saturday\" AND CURRENT_TIME.identity >= \"Jul 3, 2015 4:56:00 PM\" AND CURRENT_TIME.identity <= \"Jul 31, 2015 4:56:44 PM\") AND TRUE))",
						//result
						"<xml-fragment usergroup=\"13\"><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0</CONDITION><CONDITION exclude=\"false\" type=\"env\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"program\" value=\"PR-03\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"bafa\" value=\"BAFA-01\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"type\" value=\"pdf.nxl\" method=\"NE\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\Excel.exe\" method=\"EQ\"/></APPLICATION>",
						"<LOCATION id=\"0\"><PROPERTY name=\"GROUP\" value=\"4\" method=\"has\"/></LOCATION>",
						"<ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" value=\"6000\" method=\"GT\"/><PROPERTY name=\"CURRENT_TIME.time\" value=\"11:03:00 PM\" method=\"GE\"/><PROPERTY name=\"CURRENT_TIME.time\" value=\"12:03:16 AM\" method=\"LE\"/><PROPERTY name=\"CURRENT_TIME.weekday\" value=\"saturday\" method=\"EQ\"/><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jul 3, 2015 4:56:00 PM\" method=\"GE\"/><PROPERTY name=\"CURRENT_TIME.identity\" value=\"Jul 31, 2015 4:56:44 PM\" method=\"LE\"/></ENV>"
					},
					{ 
						"(issue 29605)",
						"BY (user.did = 10325 OR user.did = 37726 OR user.did = 37722) AND ((application.path = \"**\\Excel.exe\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\AcroRd32.exe\" AND application.publisher = \"Adobe Systems, Incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"sap ag\") OR (TRUE AND application.name = \"RMS\"))",
						"FOR (resource.fso.course = \"LAM ESC Technology\" AND resource.fso.year = \"2014\") AND (resource.fso.path = \"**.ppt**\" OR resource.fso.path = \"**.xls**\" OR resource.fso.path = \"**.doc**\")",
						"",
						//result
						"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1,2,3</CONDITION><CONDITION exclude=\"false\" type=\"usr\">0,1,2</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2,3,4,5</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"course\" value=\"LAM ESC Technology\" method=\"EQ\"/><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"path\" value=\"**.ppt**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"path\" value=\"**.xls**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"path\" value=\"**.doc**\" method=\"EQ\"/></RESOURCE></xml-fragment>",
						"<xml-fragment><USER id=\"0\"><PROPERTY name=\"did\" value=\"10325\" method=\"EQ\"/></USER><USER id=\"1\"><PROPERTY name=\"did\" value=\"37726\" method=\"EQ\"/></USER><USER id=\"2\"><PROPERTY name=\"did\" value=\"37722\" method=\"EQ\"/></USER></xml-fragment>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\Excel.exe\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Adobe Systems, Incorporated\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\POWERPNT.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"3\"><PROPERTY name=\"path\" value=\"**\\WINWORD.EXE\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"Microsoft Corporation\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"4\"><PROPERTY name=\"path\" value=\"**\\VEViewer.exe\" method=\"EQ\"/><PROPERTY name=\"publisher\" value=\"sap ag\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"5\"><PROPERTY name=\"name\" value=\"RMS\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{ 
						"(issue 30312)",
						"BY ((user.GROUP has 18 AND user.mail = \"grover.cleveland@qapf1.qalab01.nextlabs.com\") AND (application.path = \"**\\excel.exe\" OR application.path = \"**\\WINWORD.EXE\" OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR (resource.fso.sensitivity = \"Non Business\" AND resource.fso.program = \"PR-01\")",
						"",
						//result
						"<xml-fragment usergroup=\"18\"><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"usr\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2</CONDITION></xml-fragment>",
						"<RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/><PROPERTY name=\"program\" value=\"PR-01\" method=\"EQ\"/></RESOURCE>",
						"<USER id=\"0\"><PROPERTY name=\"mail\" value=\"grover.cleveland@qapf1.qalab01.nextlabs.com\" method=\"EQ\"/></USER>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**\\excel.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\WINWORD.EXE\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\AcroRd32.exe\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{ 
						"(issue 31128)",
						"BY (application.path = \"**edge.exe\" OR application.path = \"**\\veviewer.exe\" OR (application.path = \"**\\excel.exe\" OR application.path = \"**\\winword.exe\" OR application.path = \"**\\powerpnt.exe\") OR application.path = \"**\\acrord32.exe\" OR application.path = \"**VisView.exe\" OR application.path = \"**\\Siemens\\NX 8.5\\**\" OR application.path = \"**\\acrobat.exe\")",
						"FOR resource.fso.sensitivity = \"Confidential\"",
						"",
						//result
						"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0,1,2,3,4,5,6,7,8</CONDITION></xml-fragment>",
						"<RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Confidential\" method=\"EQ\"/></RESOURCE>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" value=\"**edge.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"**\\veviewer.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" value=\"**\\excel.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"3\"><PROPERTY name=\"path\" value=\"**\\winword.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"4\"><PROPERTY name=\"path\" value=\"**\\powerpnt.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"5\"><PROPERTY name=\"path\" value=\"**\\acrord32.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"6\"><PROPERTY name=\"path\" value=\"**VisView.exe\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"7\"><PROPERTY name=\"path\" value=\"**\\Siemens\\NX 8.5\\**\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"8\"><PROPERTY name=\"path\" value=\"**\\acrobat.exe\" method=\"EQ\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
			};
			// @formatter:on
		XmlOptions options = new XmlOptions();
		for (int i = 0; i < data.length; i++) {
			StringWriter out = new StringWriter();
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV4 parser = new PolicyComponentParserV4(res, users, apps, locs, envs);
			parser.processPolicy(pol, data[i][1], data[i][2], data[i][3]);
			System.out.println("Processing : " + data[i][0]);
			if (debug) {
				System.out.println("Policy: \n" + pol.toString());
				System.out.println();
				System.out.println("Resources: \n" + res.toString());
				System.out.println();
				System.out.println("Users: \n" + users.toString());
				System.out.println();
				System.out.println("Applications: \n" + apps.toString());
				System.out.println();
				System.out.println("Locations: \n" + locs.toString());
				System.out.println();
				System.out.println("Environments: \n" + envs.toString());
				System.out.println();
			}
			pol.save(out, options);
			Assert.assertEquals("Invalid policy format for row: " + (i + 1), data[i][4], out.toString());
			out = new StringWriter();
			res.save(out, options);
			Assert.assertEquals("Invalid resource format for row: " + (i + 1), data[i][5], out.toString());
			out = new StringWriter();
			users.save(out, options);
			Assert.assertEquals("Invalid users format for row: " + (i + 1), data[i][6], out.toString());
			out = new StringWriter();
			apps.save(out, options);
			Assert.assertEquals("Invalid application format for row: " + (i + 1), data[i][7], out.toString());
			out = new StringWriter();
			locs.save(out, options);
			Assert.assertEquals("Invalid location format for row: " + (i + 1), data[i][8], out.toString());
			out = new StringWriter();
			envs.save(out, options);
			Assert.assertEquals("Invalid environment format for row: " + (i + 1), data[i][9], out.toString());
		}
	}

	@Test
	public void testResource() throws IOException {
		// @formatter:off
		String[][] data = { 
			{ 
				"(A AND B) OR C",
				"BY TRUE",
				"FOR (resource.fso.ext = pdf AND resource.fso.year = 2014) OR resource.fso.ext = xls",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"ext\" value=\"xls\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"A AND (B OR C)",
				"BY TRUE",
				"FOR resource.fso.year = 2014 AND (resource.fso.ext = pdf OR resource.fso.ext = xls)",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1,2</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" value=\"xls\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A AND B) AND (C AND D)",
				"BY TRUE",
				"FOR (resource.fso.year = 2014 AND resource.fso.ext = pdf) AND (resource.fso.confidential = true AND resource.fso.tag = itar)",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/><PROPERTY name=\"confidential\" value=\"true\" method=\"EQ\"/><PROPERTY name=\"tag\" value=\"itar\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A OR B) OR (C OR D)",
				"BY TRUE",
				"FOR (resource.fso.year = 2014 OR resource.fso.year = 2015) OR (resource.fso.confidential = true OR resource.fso.tag = itar)",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" value=\"2015\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"confidential\" value=\"true\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"tag\" value=\"itar\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A AND B) OR (C AND D)",
				"BY TRUE",
				"FOR (resource.fso.year = 2014 AND resource.fso.ext = pdf) OR (resource.fso.year = 2015 AND resource.fso.ext = xls)",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" value=\"2015\" method=\"EQ\"/><PROPERTY name=\"ext\" value=\"xls\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A OR B) AND (C OR D)",
				"BY TRUE",
				"FOR (resource.fso.year = 2014 OR resource.fso.year = 2015) AND (resource.fso.ext = pdf OR resource.fso.ext = xls)",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2,3</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" value=\"2015\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"ext\" value=\"xls\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"((A OR B) OR (C AND D)",
				"BY TRUE",
				"FOR (resource.fso.year = 2014 OR resource.fso.year = 2015) OR (resource.fso.ext = pdf AND resource.fso.confidential = true)",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" value=\"2015\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/><PROPERTY name=\"confidential\" value=\"true\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"((A AND B) OR (C OR D)",
				"BY TRUE",
				"FOR (resource.fso.ext = pdf AND resource.fso.confidential = true) OR (resource.fso.year = 2014 OR resource.fso.year = 2015)",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" value=\"2015\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/><PROPERTY name=\"confidential\" value=\"true\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"((A AND B) AND D) OR (C OR D)",
				"BY TRUE",
				"FOR ((resource.fso.ext = pdf AND resource.fso.year = 2015) AND resource.fso.confidential = true) OR (resource.fso.ext = xls OR resource.fso.ext = doc)",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"ext\" value=\"xls\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"ext\" value=\"doc\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" value=\"pdf\" method=\"EQ\"/><PROPERTY name=\"year\" value=\"2015\" method=\"EQ\"/><PROPERTY name=\"confidential\" value=\"true\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A AND B) OR (C OR D OR E)",
				"BY TRUE",
				"FOR ((resource.fso.course = \"LAM ESC Technology\" AND resource.fso.year = \"2014\") OR (resource.fso.path = \"**.xls**\" OR resource.fso.path = \"**.ppt**\" OR resource.fso.path = \"**.doc**\"))",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"path\" value=\"**.xls**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"path\" value=\"**.ppt**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"path\" value=\"**.doc**\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"course\" value=\"LAM ESC Technology\" method=\"EQ\"/><PROPERTY name=\"year\" value=\"2014\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A OR B) AND NOT (C OR D)",
				"BY TRUE",
				"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"General Business\") AND NOT ((resource.fso.program = \"PR-01\" OR resource.fso.ear = \"EAR-01\")))",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION><CONDITION exclude=\"false\" type=\"res\">3</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" value=\"General Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"ear\" value=\"EAR-01\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"(A OR B) AND NOT C AND (C OR D)",
				"BY TRUE",
				"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\") AND NOT (resource.fso.program = \"PR-01\") AND (resource.fso.ear = \"EAR-01\" OR resource.fso.bafa = \"BAFA-02\"))",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION><CONDITION exclude=\"false\" type=\"res\">3,4</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" value=\"Non Business\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" value=\"Confidential\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"ear\" value=\"EAR-01\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"4\"><PROPERTY name=\"bafa\" value=\"BAFA-02\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
		};
		// @formatter:on
		for (int i = 0; i < data.length; i++) {
			StringWriter out = new StringWriter();
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV4 parser = new PolicyComponentParserV4(res, users, apps, locs, envs);
			parser.processPolicy(pol, data[i][1], data[i][2], data[i][3]);
			System.out.println("Processing : " + data[i][0]);
			if (debug) {
				System.out.println("Policy: \n" + pol.toString());
				System.out.println();
				System.out.println("Resources: \n" + res.toString());
				System.out.println();
				System.out.println("Users: \n" + users.toString());
				System.out.println();
				System.out.println("Applications: \n" + apps.toString());
				System.out.println();
				System.out.println("Locations: \n" + locs.toString());
				System.out.println();
				System.out.println("Environments: \n" + envs.toString());
				System.out.println();
			}
			pol.save(out, options);
			Assert.assertEquals("Invalid policy format for row: " + (i + 1), data[i][4], out.toString());
			out = new StringWriter();
			res.save(out, options);
			Assert.assertEquals("Invalid resource format for row: " + (i + 1), data[i][5], out.toString());
			out = new StringWriter();
			users.save(out, options);
			Assert.assertEquals("Invalid users format for row: " + (i + 1), data[i][6], out.toString());
			out = new StringWriter();
			apps.save(out, options);
			Assert.assertEquals("Invalid application format for row: " + (i + 1), data[i][7], out.toString());
			out = new StringWriter();
			locs.save(out, options);
			Assert.assertEquals("Invalid location format for row: " + (i + 1), data[i][8], out.toString());
			out = new StringWriter();
			envs.save(out, options);
			Assert.assertEquals("Invalid environment format for row: " + (i + 1), data[i][9], out.toString());
		}
	}

	@Test
	public void testResourceOnly() throws IOException {
		// @formatter:off
		String[][] data = {
			{
				"FOR TRUE",
				"BY TRUE",
				"",
				//result
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR resource.fso.name1 = val1",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR resource.fso.name1 = val1 AND resource.fso.name2 = val2",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 AND (resource.fso.name2 = val2 AND resource.fso.name3 = val3))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND (resource.fso.name3 = val3 AND resource.fso.name4 = val4))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 AND ((resource.fso.name2 = val2 AND resource.fso.name3 = val3) AND (resource.fso.name4 = val4 AND resource.fso.name5 = val5)))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/><PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND ((resource.fso.name3 = val3 AND resource.fso.name4 = val4) AND (resource.fso.name5 = val5 AND resource.fso.name6 = val6)))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/><PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/><PROPERTY name=\"name6\" value=\"val6\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 OR resource.fso.name2 = val2 OR (resource.fso.name3 = val3))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) OR (resource.fso.name3 = val3 AND resource.fso.name4 = val4))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 OR ((resource.fso.name2 = val2 AND resource.fso.name3 = val3) AND (resource.fso.name4 = val4 AND resource.fso.name4 = val4)))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) OR ((resource.fso.name3 = val3 AND resource.fso.name4 = val4) AND (resource.fso.name5 = val5 AND resource.fso.name6 = val6)))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/><PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/><PROPERTY name=\"name6\" value=\"val6\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR NOT(resource.fso.name1 = val1)",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"NE\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR NOT(resource.fso.name1 = val1 AND resource.fso.name2 = val2)",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"NE\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1) AND NOT((resource.fso.name2 = val2 AND resource.fso.name3 = val3)))",
				"BY TRUE",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1,2</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"NE\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 OR ((resource.fso.name2 = val2) AND NOT((resource.fso.name3 = val3 AND resource.fso.name3 = val3))))",
				"BY TRUE",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2,3</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name3\" value=\"val3\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 AND ((resource.fso.name2 = val2) AND NOT((resource.fso.name3 = val3 AND resource.fso.name4 = val4))))",
				"BY TRUE",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2,3</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name4\" value=\"val4\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) OR ((resource.fso.name3 = val3) AND NOT((resource.fso.name4 = val4 AND resource.fso.name5 = val5))))",
				"BY TRUE",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2,3</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name4\" value=\"val4\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name5\" value=\"val5\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND ((resource.fso.name3 = val3) AND NOT((resource.fso.name4 = val4 AND resource.fso.name5 = val5))))",
				"BY TRUE",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2,3</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name4\" value=\"val4\" method=\"NE\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name5\" value=\"val5\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (NOT(resource.fso.name1 = val1 AND NOT ((resource.fso.name2 = val2)) AND resource.fso.name3 = val3))",
				"BY TRUE",
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"NE\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"NE\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR ((resource.fso.name1 = val1 AND resource.fso.name2 = val2) AND NOT (((resource.fso.name3 = val3) AND NOT((resource.fso.name4 = val4 AND resource.fso.name5 = val5)))))",
				"BY TRUE",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0</CONDITION><CONDITION exclude=\"false\" type=\"res\">1,2</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" value=\"val3\" method=\"NE\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/><PROPERTY name=\"name5\" value=\"val5\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
		};
		// @formatter:on
		for (int i = 0; i < data.length; i++) {
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV4 parser = new PolicyComponentParserV4(res, users, apps, locs, envs);
			System.out.println("Processing record: " + (i + 1));
			parser.processPolicy(pol, data[i][0], data[i][1], data[i][2]);
			if (debug) {
				System.out.println("Policy: \n" + pol.toString());
				System.out.println();
				System.out.println("Resources: \n" + res.toString());
				System.out.println();
				System.out.println("Users: \n" + users.toString());
				System.out.println();
				System.out.println("Applications: \n" + apps.toString());
				System.out.println();
				System.out.println("Locations: \n" + locs.toString());
				System.out.println();
				System.out.println("Environments: \n" + envs.toString());
				System.out.println();
			}
			StringWriter out = new StringWriter();
			pol.save(out, options);
			Assert.assertEquals("Invalid policy format for row: " + (i + 1), data[i][3], out.toString());

			out = new StringWriter();
			res.save(out, options);
			Assert.assertEquals("Invalid resource format for row: " + (i + 1), data[i][4], out.toString());

			out = new StringWriter();
			users.save(out, options);
			Assert.assertEquals("Invalid users format for row: " + (i + 1), data[i][5], out.toString());

			out = new StringWriter();
			apps.save(out, options);
			Assert.assertEquals("Invalid application format for row: " + (i + 1), data[i][6], out.toString());

			out = new StringWriter();
			locs.save(out, options);
			Assert.assertEquals("Invalid location format for row: " + (i + 1), data[i][7], out.toString());

			out = new StringWriter();
			envs.save(out, options);
			Assert.assertEquals("Invalid environment format for row: " + (i + 1), data[i][8], out.toString());
		}
	}

	@Test
	public void testSubject() throws IOException {
		// @formatter:off
		String[][] data = {
			{
				"FOR TRUE",
				"BY (user.GROUP has 0 AND ((application.prop = \"prop\" AND application.pro = \"pro\") AND application.blah1 = \"blah1\" AND (application.prop1 = \"prop1\" AND application.prop2 = \"prop2\") AND ((application.prop3 = \"2.45AM\" AND application.prop4 = \"2.45 AM\") AND application.blah = \"blah\")))",
				"",
				//result
				"<xml-fragment usergroup=\"0\"><CONDITION exclude=\"false\" type=\"app\">0</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<APPLICATION id=\"0\"><PROPERTY name=\"prop\" value=\"prop\" method=\"EQ\"/><PROPERTY name=\"pro\" value=\"pro\" method=\"EQ\"/><PROPERTY name=\"blah1\" value=\"blah1\" method=\"EQ\"/><PROPERTY name=\"prop1\" value=\"prop1\" method=\"EQ\"/><PROPERTY name=\"prop2\" value=\"prop2\" method=\"EQ\"/><PROPERTY name=\"prop3\" value=\"2.45AM\" method=\"EQ\"/><PROPERTY name=\"prop4\" value=\"2.45 AM\" method=\"EQ\"/><PROPERTY name=\"blah\" value=\"blah\" method=\"EQ\"/></APPLICATION>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR TRUE",
				"BY (user.u1 = v1 OR ((application.n1 = V1 AND application.n2 > V2 AND application.n3 = V3 AND application.\"name with space\" != \"value with space\") OR (user.name = nameVal AND user.\"loc with space\" <= \"San Mateo\")) OR (host.h1 = hh AND host.h2 = \"with space\"))",
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"usr\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"app\">0</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment><USER id=\"0\"><PROPERTY name=\"u1\" value=\"v1\" method=\"EQ\"/></USER><USER id=\"1\"><PROPERTY name=\"name\" value=\"nameVal\" method=\"EQ\"/><PROPERTY name=\"loc with space\" value=\"San Mateo\" method=\"LE\"/></USER></xml-fragment>",
				"<APPLICATION id=\"0\"><PROPERTY name=\"n1\" value=\"V1\" method=\"EQ\"/><PROPERTY name=\"n2\" value=\"V2\" method=\"GT\"/><PROPERTY name=\"n3\" value=\"V3\" method=\"EQ\"/><PROPERTY name=\"name with space\" value=\"value with space\" method=\"NE\"/></APPLICATION>",
				"<LOCATION id=\"0\"><PROPERTY name=\"h1\" value=\"hh\" method=\"EQ\"/><PROPERTY name=\"h2\" value=\"with space\" method=\"EQ\"/></LOCATION>",
				"<xml-fragment/>",
			},
			{
				"FOR TRUE",
				"BY user.Group has 0 OR host.name = prince AND application.name = ACROBAT OR application.path = Drive",
				"",
				//result
				"<xml-fragment usergroup=\"0\"><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">0</CONDITION><CONDITION exclude=\"false\" type=\"app\">1</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"name\" value=\"ACROBAT\" method=\"EQ\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" value=\"Drive\" method=\"EQ\"/></APPLICATION></xml-fragment>",
				"<LOCATION id=\"0\"><PROPERTY name=\"name\" value=\"prince\" method=\"EQ\"/></LOCATION>", "<xml-fragment/>", "<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR TRUE",
				"BY user.Group has 0",
				"",
				//result
				"<xml-fragment usergroup=\"0\"/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 OR resource.fso.name3 = val3 OR resource.fso.name4 = val4",
				"BY TRUE", 
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR resource.fso.name = \"**.jt\"",
				"BY TRUE", 
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name\" value=\"**.jt\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 AND resource.fso.name3 = val3 AND resource.fso.name4 = val4",
				"BY TRUE", 
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR resource.fso.name1 = val1 OR resource.fso.name2 = val2 AND resource.fso.name3 = val3 AND resource.fso.name4 = val4",
				"BY TRUE", 
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" value=\"val1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" value=\"val2\" method=\"EQ\"/><PROPERTY name=\"name3\" value=\"val3\" method=\"EQ\"/><PROPERTY name=\"name4\" value=\"val4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR (resource.fso.comp3 = \"property1\" AND resource.fso.comp3 = \"property2\" AND resource.fso.comp3 = \"property3\")",
				"BY TRUE", 
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"comp3\" value=\"property1\" method=\"EQ\"/><PROPERTY name=\"comp3\" value=\"property2\" method=\"EQ\"/><PROPERTY name=\"comp3\" value=\"property3\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR ((resource.fso.comp3 = \"property1\" OR resource.fso.comp3 = \"property2\") AND (resource.fso.comp3 = \"property3\" AND resource.fso.comp4 = \"property4\"))",
				"BY TRUE", 
				"",
				//result
				"<xml-fragment><CONDITION exclude=\"false\" type=\"res\">0,1</CONDITION><CONDITION exclude=\"false\" type=\"res\">2</CONDITION></xml-fragment>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"comp3\" value=\"property1\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"comp3\" value=\"property2\" method=\"EQ\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"comp3\" value=\"property3\" method=\"EQ\"/><PROPERTY name=\"comp4\" value=\"property4\" method=\"EQ\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR (resource.fso.program = \"PR-01\" AND resource.fso.bafa = \"BAFA-01\")",
				"BY TRUE", 
				"",
				//result
				"<CONDITION exclude=\"false\" type=\"res\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"program\" value=\"PR-01\" method=\"EQ\"/><PROPERTY name=\"bafa\" value=\"BAFA-01\" method=\"EQ\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
			{
				"FOR TRUE",
				"BY ((user.locationcode = \"SG\" AND user.user_location_code = \"IN\") AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\") AND (user.GROUP has 2 AND (user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\")))",
				"",
				//result
				"<xml-fragment usergroup=\"2\"><CONDITION exclude=\"false\" type=\"usr\">0</CONDITION><CONDITION exclude=\"false\" type=\"loc\">0</CONDITION><CONDITION exclude=\"false\" type=\"usr\">1</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment><USER id=\"0\"><PROPERTY name=\"locationcode\" value=\"SG\" method=\"EQ\"/><PROPERTY name=\"user_location_code\" value=\"IN\" method=\"EQ\"/></USER><USER id=\"1\"><PROPERTY name=\"dummyproperty1\" value=\"dummy\" method=\"EQ\"/><PROPERTY name=\"dummyproperty2\" value=\"dummy2\" method=\"EQ\"/></USER></xml-fragment>",
				"<xml-fragment/>", 
				"<LOCATION id=\"0\"><PROPERTY name=\"inet_address\" value=\"10.63.1.144\" method=\"EQ\"/><PROPERTY name=\"inet_address\" value=\"10.63.0.101\" method=\"EQ\"/></LOCATION>",
				"<xml-fragment/>", 
			},
		};
		// @formatter:on
		for (int i = 0; i < data.length; i++) {
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV4 parser = new PolicyComponentParserV4(res, users, apps, locs, envs);
			System.out.println("Processing record: " + (i + 1));
			parser.processPolicy(pol, data[i][0], data[i][1], data[i][2]);
			if (debug) {
				System.out.println("Policy: \n" + pol.toString());
				System.out.println();
				System.out.println("Resources: \n" + res.toString());
				System.out.println();
				System.out.println("Users: \n" + users.toString());
				System.out.println();
				System.out.println("Applications: \n" + apps.toString());
				System.out.println();
				System.out.println("Locations: \n" + locs.toString());
				System.out.println();
				System.out.println("Environments: \n" + envs.toString());
				System.out.println();
			}
			StringWriter out = new StringWriter();
			pol.save(out, options);
			Assert.assertEquals("Invalid policy format for row: " + (i + 1), data[i][3], out.toString());

			out = new StringWriter();
			res.save(out, options);
			Assert.assertEquals("Invalid resource format for row: " + (i + 1), data[i][4], out.toString());

			out = new StringWriter();
			users.save(out, options);
			Assert.assertEquals("Invalid users format for row: " + (i + 1), data[i][5], out.toString());

			out = new StringWriter();
			apps.save(out, options);
			Assert.assertEquals("Invalid application format for row: " + (i + 1), data[i][6], out.toString());

			out = new StringWriter();
			locs.save(out, options);
			Assert.assertEquals("Invalid location format for row: " + (i + 1), data[i][7], out.toString());

			out = new StringWriter();
			envs.save(out, options);
			Assert.assertEquals("Invalid environment format for row: " + (i + 1), data[i][8], out.toString());
		}
	}
	
	@Test
	public void testUnsupportedElement() {
		POLICY pol = POLICY.Factory.newInstance();
		RESOURCES res = RESOURCES.Factory.newInstance();
		USERS users = USERS.Factory.newInstance();
		APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
		LOCATIONS locs = LOCATIONS.Factory.newInstance();
		ENVS envs = ENVS.Factory.newInstance();
		PolicyComponentParserV4 parser = new PolicyComponentParserV4(res, users, apps, locs, envs);
		String resourceStr = "FOR resource.portal.name = \"testdoc1\"";
		String subjectStr = "BY TRUE";
		String envStr = "";
		exception.expect(UnsupportedComponentException.class);
		parser.processPolicy(pol, resourceStr, subjectStr, envStr);
	}
}
