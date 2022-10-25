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
import org.junit.Test;

import com.nextlabs.rms.services.manager.PolicyComponentParserV5;

public class NewPolicyComponentParserTest5 {
	private XmlOptions options;
	private boolean debug;

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
					"BY TRUE",
					"FOR TRUE",
					"WHERE (TRUE AND (TRUE AND TRUE))",
					//result
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>"
				},
				{ 
					"BY TRUE",
					"FOR TRUE",
					"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND (CURRENT_TIME.weekday = \"sunday\" OR CURRENT_TIME.weekday = \"monday\" OR CURRENT_TIME.weekday = \"tuesday\" OR CURRENT_TIME.weekday = \"wednesday\" OR CURRENT_TIME.weekday = \"thursday\" OR CURRENT_TIME.weekday = \"friday\" OR CURRENT_TIME.weekday = \"saturday\") AND ENVIRONMENT.REMOTE_ACCESS = 1) AND TRUE))",
					//result
					"<CONDITION type=\"env\" exclude=\"false\">0,1,2,3,4,5,6</CONDITION>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment><ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"sunday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"1\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"monday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"2\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"tuesday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"3\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"wednesday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"4\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"thursday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"5\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"friday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"6\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"saturday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV></xml-fragment>"
				},
				{ 
					"BY TRUE",
					"FOR TRUE",
					"WHERE (TRUE AND (TRUE AND (user.licence = resource.fso.licence AND (resource.fso.prop1 = user.prop1 AND NOT (resource.fso.prop2 = user.prop2)))))",
					//result
					"<xml-fragment><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"res\" exclude=\"false\">0</CONDITION></xml-fragment>",
					"<RESOURCE id=\"0\"><PROPERTY name=\"prop1\" method=\"EQ\" value=\"user.prop1\"/><PROPERTY name=\"prop2\" method=\"NE\" value=\"user.prop2\"/></RESOURCE>",
					"<USER id=\"0\"><PROPERTY name=\"licence\" method=\"EQ\" value=\"resource.fso.licence\"/></USER>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>"
				},
				{ 
					"BY TRUE",
					"FOR TRUE",
					"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND (CURRENT_TIME.weekday = \"sunday\" OR CURRENT_TIME.weekday = \"monday\" OR CURRENT_TIME.weekday = \"tuesday\" OR CURRENT_TIME.weekday = \"wednesday\" OR CURRENT_TIME.weekday = \"thursday\" OR CURRENT_TIME.weekday = \"friday\" OR CURRENT_TIME.weekday = \"saturday\") AND ENVIRONMENT.REMOTE_ACCESS = 1) AND (user.name = resource.fso.name AND resource.fso.name = user.name)))",
					//result
					"<xml-fragment><CONDITION type=\"env\" exclude=\"false\">0,1,2,3,4,5,6</CONDITION><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"res\" exclude=\"false\">0</CONDITION></xml-fragment>",
					"<RESOURCE id=\"0\"><PROPERTY name=\"name\" method=\"EQ\" value=\"user.name\"/></RESOURCE>",
					"<USER id=\"0\"><PROPERTY name=\"name\" method=\"EQ\" value=\"resource.fso.name\"/></USER>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment><ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"sunday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"1\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"monday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"2\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"tuesday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"3\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"wednesday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"4\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"thursday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"5\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"friday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"6\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"saturday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV></xml-fragment>"
				},
				{ 
					"BY TRUE",
					"FOR TRUE",
					"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND (CURRENT_TIME.weekday = \"sunday\" OR CURRENT_TIME.weekday = \"monday\" OR CURRENT_TIME.weekday = \"tuesday\" OR CURRENT_TIME.weekday = \"wednesday\" OR CURRENT_TIME.weekday = \"thursday\" OR CURRENT_TIME.weekday = \"friday\" OR CURRENT_TIME.weekday = \"saturday\") AND ENVIRONMENT.REMOTE_ACCESS = 1) AND ((user.prop1 = resource.fso.val1 AND user.prop2 = application.val1 AND NOT ((user.prop3 = host.val1 OR user.prop4 = resource.fso.val2))) AND resource.fso.prop2 = user.val1)))",
					//result
					"<xml-fragment><CONDITION type=\"env\" exclude=\"false\">0,1,2,3,4,5,6</CONDITION><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"res\" exclude=\"false\">0</CONDITION></xml-fragment>",
					"<RESOURCE id=\"0\"><PROPERTY name=\"prop2\" method=\"EQ\" value=\"user.val1\"/></RESOURCE>",
					"<USER id=\"0\"><PROPERTY name=\"prop1\" method=\"EQ\" value=\"resource.fso.val1\"/><PROPERTY name=\"prop2\" method=\"EQ\" value=\"application.val1\"/><PROPERTY name=\"prop3\" method=\"NE\" value=\"host.val1\"/><PROPERTY name=\"prop4\" method=\"NE\" value=\"resource.fso.val2\"/></USER>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment><ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"sunday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"1\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"monday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"2\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"tuesday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"3\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"wednesday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"4\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"thursday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"5\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"friday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV><ENV id=\"6\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"saturday\"/><PROPERTY name=\"ENVIRONMENT.REMOTE_ACCESS\" method=\"EQ\" value=\"1\"/></ENV></xml-fragment>"
				}
				
		};
		// @formatter:on
		XmlOptions options = new XmlOptions();
//		debug = true;
		for (int i = 0; i < data.length; i++) {
			StringWriter out = new StringWriter();
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV5 parser = new PolicyComponentParserV5(res, users, apps, locs, envs);
			parser.processPolicy(pol, data[i][1], data[i][0], data[i][2]);
			System.out.println("Processing Policy "+i+" : " + data[i][0]);
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
		System.out.println("Done");
	}

	@Test
	public void testMixComponent() throws IOException {
		// @formatter:off
			String[][] data = { 
					{
						"(issue 30731)",
						"BY ((user.did = 10325 OR user.did = 37722 OR user.did = 37724) AND ((application.path = \"**\\Excel.exe\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\AcroRd32.exe\" AND application.publisher = \"Adobe Systems, Incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"sap ag\")))",
						"FOR (resource.fso.course = \"2300 Conductor Etch Process\" AND resource.fso.year = \"2014\") AND (resource.fso.path = \"**.ppt**\" OR resource.fso.path = \"**.xls**\" OR resource.fso.path = \"**.doc**\")",
						"",
						//result
						"<xml-fragment><CONDITION type=\"usr\" exclude=\"false\">0,1,2</CONDITION><CONDITION type=\"app\" exclude=\"false\">0,1,2,3</CONDITION><CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"course\" method=\"EQ\" value=\"2300 Conductor Etch Process\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"path\" method=\"EQ\" value=\"**.ppt**\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"course\" method=\"EQ\" value=\"2300 Conductor Etch Process\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"path\" method=\"EQ\" value=\"**.xls**\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"course\" method=\"EQ\" value=\"2300 Conductor Etch Process\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"path\" method=\"EQ\" value=\"**.doc**\"/></RESOURCE></xml-fragment>",
						"<xml-fragment><USER id=\"0\"><PROPERTY name=\"did\" method=\"EQ\" value=\"10325\"/></USER><USER id=\"1\"><PROPERTY name=\"did\" method=\"EQ\" value=\"37722\"/></USER><USER id=\"2\"><PROPERTY name=\"did\" method=\"EQ\" value=\"37724\"/></USER></xml-fragment>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\Excel.exe\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\AcroRd32.exe\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Adobe Systems, Incorporated\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\POWERPNT.EXE\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"3\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\VEViewer.exe\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"sap ag\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{
						"(issue 30328)",
						"BY ((host.GROUP has 4 OR host.inet_address = \"10.23.57.135\") AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\") AND NOT (resource.fso.program = \"PR-01\") AND (resource.fso.ear = \"EAR-01\" OR resource.fso.bafa = \"BAFA-02\"))",
						"WHERE (TRUE AND ((CURRENT_TIME.identity >= \"Jun 23, 2015 5:27:30 PM\" AND ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 600) AND TRUE))",
						//result
						"<xml-fragment><CONDITION type=\"loc\" exclude=\"false\">0,1</CONDITION><CONDITION type=\"app\" exclude=\"false\">0,1,2</CONDITION><CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION><CONDITION type=\"env\" exclude=\"false\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"EQ\" value=\"EAR-01\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"EQ\" value=\"EAR-01\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\excel.exe\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\WINWORD.EXE\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\AcroRd32.exe\"/></APPLICATION></xml-fragment>",
						"<xml-fragment><LOCATION id=\"0\"><PROPERTY name=\"GROUP\" method=\"has\" value=\"4\"/></LOCATION><LOCATION id=\"1\"><PROPERTY name=\"inet_address\" method=\"EQ\" value=\"10.23.57.135\"/></LOCATION></xml-fragment>",
						"<ENV id=\"0\"><PROPERTY name=\"CURRENT_TIME.identity\" method=\"GE\" value=\"Jun 23, 2015 5:27:30 PM\"/><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"600\"/></ENV>"
					},
					{
						"(issue 30362)",
						"BY ((host.GROUP has 5 AND host.inet_address = \"10.23.56.55\") AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\" OR resource.fso.bafa = \"BAFA-02\") AND (resource.fso.type = \"doc.nxl\" OR resource.fso.type = \"docx.nxl\" OR resource.fso.type = \"xls.nxl\"))",
						"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 180 AND CURRENT_TIME.identity >= \"Jun 21, 2015 5:27:30 PM\") AND TRUE))",
						//result
						"<xml-fragment><CONDITION type=\"loc\" exclude=\"false\">0</CONDITION><CONDITION type=\"app\" exclude=\"false\">0,1,2</CONDITION><CONDITION type=\"res\" exclude=\"false\">0,1,2,3,4,5,6,7,8</CONDITION><CONDITION type=\"env\" exclude=\"false\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"doc.nxl\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"docx.nxl\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"xls.nxl\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"doc.nxl\"/></RESOURCE><RESOURCE id=\"4\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"docx.nxl\"/></RESOURCE><RESOURCE id=\"5\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"xls.nxl\"/></RESOURCE><RESOURCE id=\"6\"><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"doc.nxl\"/></RESOURCE><RESOURCE id=\"7\"><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"docx.nxl\"/></RESOURCE><RESOURCE id=\"8\"><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/><PROPERTY name=\"type\" method=\"EQ\" value=\"xls.nxl\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\excel.exe\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\WINWORD.EXE\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\AcroRd32.exe\"/></APPLICATION></xml-fragment>",
                        "<LOCATION id=\"0\"><PROPERTY name=\"GROUP\" method=\"has\" value=\"5\"/><PROPERTY name=\"inet_address\" method=\"EQ\" value=\"10.23.56.55\"/></LOCATION>",
						"<ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"180\"/><PROPERTY name=\"CURRENT_TIME.identity\" method=\"GE\" value=\"Jun 21, 2015 5:27:30 PM\"/></ENV>"
					},
					{
						"(issue 30414)",
						"BY (user.GROUP has 21 AND (application.path = \"**\\excel.exe\" OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"General Business\") AND NOT ((resource.fso.program = \"PR-01\" OR resource.fso.ear = \"EAR-01\")))",
						"",
						//result
						"<xml-fragment usergroup=\"21\"><CONDITION type=\"app\" exclude=\"false\">0,1,2</CONDITION><CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"NE\" value=\"EAR-01\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"General Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"NE\" value=\"EAR-01\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\excel.exe\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\WINWORD.EXE\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\AcroRd32.exe\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{
						"(issue 30416)",
						"BY ((user.givenname = \"a\" AND user.telephonenumber = \"64292110\") AND NOT (application.path = \"**\\acrord32.exe\"))",
						"FOR resource.fso.department = \"Executive Management\"",
						"",
						//result
						"<xml-fragment><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"app\" exclude=\"false\">0</CONDITION><CONDITION type=\"res\" exclude=\"false\">0</CONDITION></xml-fragment>",
						"<RESOURCE id=\"0\"><PROPERTY name=\"department\" method=\"EQ\" value=\"Executive Management\"/></RESOURCE>",
						"<USER id=\"0\"><PROPERTY name=\"givenname\" method=\"EQ\" value=\"a\"/><PROPERTY name=\"telephonenumber\" method=\"EQ\" value=\"64292110\"/></USER>",
						"<APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"NE\" value=\"**\\acrord32.exe\"/></APPLICATION>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{
						"(issue 30406)",
						"BY (user.GROUP has 13 AND host.GROUP has 4 AND application.path = \"**\\Excel.exe\")",
						"FOR ((resource.fso.program = \"PR-03\" OR resource.fso.bafa = \"BAFA-01\") AND NOT (resource.fso.type = \"pdf.nxl\"))",
						"WHERE (TRUE AND ((ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT > 6000 AND CURRENT_TIME.time >= \"11:03:00 PM\" AND CURRENT_TIME.time <= \"12:03:16 AM\" AND CURRENT_TIME.weekday = \"saturday\" AND CURRENT_TIME.identity >= \"Jul 3, 2015 4:56:00 PM\" AND CURRENT_TIME.identity <= \"Jul 31, 2015 4:56:44 PM\") AND TRUE))",
						//result
						"<xml-fragment usergroup=\"13\"><CONDITION type=\"loc\" exclude=\"false\">0</CONDITION><CONDITION type=\"app\" exclude=\"false\">0</CONDITION><CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION><CONDITION type=\"env\" exclude=\"false\">0</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"program\" method=\"EQ\" value=\"PR-03\"/><PROPERTY name=\"type\" method=\"NE\" value=\"pdf.nxl\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-01\"/><PROPERTY name=\"type\" method=\"NE\" value=\"pdf.nxl\"/></RESOURCE></xml-fragment>",
						"<xml-fragment/>",
						"<APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\Excel.exe\"/></APPLICATION>",
						"<LOCATION id=\"0\"><PROPERTY name=\"GROUP\" method=\"has\" value=\"4\"/></LOCATION>",
						"<ENV id=\"0\"><PROPERTY name=\"ENVIRONMENT.TIME_SINCE_LAST_HEARTBEAT\" method=\"GT\" value=\"6000\"/><PROPERTY name=\"CURRENT_TIME.time\" method=\"GE\" value=\"11:03:00 PM\"/><PROPERTY name=\"CURRENT_TIME.time\" method=\"LE\" value=\"12:03:16 AM\"/><PROPERTY name=\"CURRENT_TIME.weekday\" method=\"EQ\" value=\"saturday\"/><PROPERTY name=\"CURRENT_TIME.identity\" method=\"GE\" value=\"Jul 3, 2015 4:56:00 PM\"/><PROPERTY name=\"CURRENT_TIME.identity\" method=\"LE\" value=\"Jul 31, 2015 4:56:44 PM\"/></ENV>"
					},
                    {
						"(issue 29605)",
						"BY ((user.did = 10325 OR user.did = 37726 OR user.did = 37722) AND ((application.path = \"**\\Excel.exe\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\AcroRd32.exe\" AND application.publisher = \"Adobe Systems, Incorporated\") OR (application.path = \"**\\POWERPNT.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\WINWORD.EXE\" AND application.publisher = \"Microsoft Corporation\") OR (application.path = \"**\\VEViewer.exe\" AND application.publisher = \"sap ag\") OR (application.name = \"RMS\")))",
						"FOR (resource.fso.course = \"LAM ESC Technology\" AND resource.fso.year = \"2014\") AND (resource.fso.path = \"**.ppt**\" OR resource.fso.path = \"**.xls**\" OR resource.fso.path = \"**.doc**\")",
						"",
						//result
						"<xml-fragment><CONDITION type=\"usr\" exclude=\"false\">0,1,2</CONDITION><CONDITION type=\"app\" exclude=\"false\">0,1,2,3,4,5</CONDITION><CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION></xml-fragment>",
						"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"course\" method=\"EQ\" value=\"LAM ESC Technology\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"path\" method=\"EQ\" value=\"**.ppt**\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"course\" method=\"EQ\" value=\"LAM ESC Technology\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"path\" method=\"EQ\" value=\"**.xls**\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"course\" method=\"EQ\" value=\"LAM ESC Technology\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"path\" method=\"EQ\" value=\"**.doc**\"/></RESOURCE></xml-fragment>",
						"<xml-fragment><USER id=\"0\"><PROPERTY name=\"did\" method=\"EQ\" value=\"10325\"/></USER><USER id=\"1\"><PROPERTY name=\"did\" method=\"EQ\" value=\"37726\"/></USER><USER id=\"2\"><PROPERTY name=\"did\" method=\"EQ\" value=\"37722\"/></USER></xml-fragment>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\Excel.exe\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\AcroRd32.exe\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Adobe Systems, Incorporated\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\POWERPNT.EXE\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"3\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\WINWORD.EXE\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"Microsoft Corporation\"/></APPLICATION><APPLICATION id=\"4\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\VEViewer.exe\"/><PROPERTY name=\"publisher\" method=\"EQ\" value=\"sap ag\"/></APPLICATION><APPLICATION id=\"5\"><PROPERTY name=\"name\" method=\"EQ\" value=\"RMS\"/></APPLICATION></xml-fragment>",
						"<xml-fragment/>",
						"<xml-fragment/>"
					},
					{
						"(issue 30312)",
						"BY ((user.GROUP has 18 AND user.mail = \"grover.cleveland@qapf1.qalab01.nextlabs.com\") AND (application.path = \"**\\excel.exe\" OR application.path = \"**\\WINWORD.EXE\" OR application.path = \"**\\AcroRd32.exe\"))",
						"FOR (resource.fso.sensitivity = \"Non Business\" AND resource.fso.program = \"PR-01\")",
						"",
						//result
						"<xml-fragment usergroup=\"18\"><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"app\" exclude=\"false\">0,1,2</CONDITION><CONDITION type=\"res\" exclude=\"false\">0</CONDITION></xml-fragment>",
						"<RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"EQ\" value=\"PR-01\"/></RESOURCE>",
						"<USER id=\"0\"><PROPERTY name=\"mail\" method=\"EQ\" value=\"grover.cleveland@qapf1.qalab01.nextlabs.com\"/></USER>",
						"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\excel.exe\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\WINWORD.EXE\"/></APPLICATION><APPLICATION id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**\\AcroRd32.exe\"/></APPLICATION></xml-fragment>",
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
			PolicyComponentParserV5 parser = new PolicyComponentParserV5(res, users, apps, locs, envs);
			parser.processPolicy(pol, data[i][2], data[i][1], data[i][3]);
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
				"BY TRUE",
				"FOR ((resource.fso.ext = pdf AND resource.fso.year = 2014) OR resource.fso.ext = xls)",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"xls\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR (resource.fso.year = 2014 AND (resource.fso.ext = pdf OR resource.fso.ext = xls))",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"xls\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR ((resource.fso.year = 2014 AND resource.fso.ext = pdf) AND (resource.fso.confidential = true AND resource.fso.tag = itar))",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/><PROPERTY name=\"confidential\" method=\"EQ\" value=\"true\"/><PROPERTY name=\"tag\" method=\"EQ\" value=\"itar\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR ((resource.fso.year = 2014 OR resource.fso.year = 2015) OR (resource.fso.confidential = true OR resource.fso.tag = itar))",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"confidential\" method=\"EQ\" value=\"true\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"tag\" method=\"EQ\" value=\"itar\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR (resource.fso.year = 2014 AND resource.fso.ext = pdf) OR (resource.fso.year = 2015 AND resource.fso.ext = xls)",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"xls\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR (resource.fso.year = 2014 OR resource.fso.year = 2015) AND (resource.fso.ext = pdf OR resource.fso.ext = xls)",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"xls\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/><PROPERTY name=\"ext\" method=\"EQ\" value=\"xls\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR (resource.fso.year = 2014 OR resource.fso.year = 2015) OR (resource.fso.ext = pdf AND resource.fso.confidential = true)",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/><PROPERTY name=\"confidential\" method=\"EQ\" value=\"true\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR (resource.fso.ext = pdf AND resource.fso.confidential = true) OR (resource.fso.year = 2014 OR resource.fso.year = 2015)",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/><PROPERTY name=\"confidential\" method=\"EQ\" value=\"true\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR ((resource.fso.ext = pdf AND resource.fso.year = 2015) AND resource.fso.confidential = true) OR (resource.fso.ext = xls OR resource.fso.ext = doc)",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"pdf\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2015\"/><PROPERTY name=\"confidential\" method=\"EQ\" value=\"true\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"xls\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"ext\" method=\"EQ\" value=\"doc\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR ((resource.fso.course = \"LAM ESC Technology\" AND resource.fso.year = \"2014\") OR (resource.fso.path = \"**.xls**\" OR resource.fso.path = \"**.ppt**\" OR resource.fso.path = \"**.doc**\"))",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"course\" method=\"EQ\" value=\"LAM ESC Technology\"/><PROPERTY name=\"year\" method=\"EQ\" value=\"2014\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**.xls**\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**.ppt**\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"path\" method=\"EQ\" value=\"**.doc**\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"General Business\") AND NOT ((resource.fso.program = \"PR-01\" OR resource.fso.ear = \"EAR-01\")))",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"NE\" value=\"EAR-01\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"General Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"NE\" value=\"EAR-01\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
			{ 
				"BY TRUE",
				"FOR ((resource.fso.sensitivity = \"Non Business\" OR resource.fso.sensitivity = \"Confidential\") AND NOT (resource.fso.program = \"PR-01\") AND (resource.fso.ear = \"EAR-01\" OR resource.fso.bafa = \"BAFA-02\"))",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"EQ\" value=\"EAR-01\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Non Business\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"ear\" method=\"EQ\" value=\"EAR-01\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"sensitivity\" method=\"EQ\" value=\"Confidential\"/><PROPERTY name=\"program\" method=\"NE\" value=\"PR-01\"/><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-02\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>"
			},
		};
//		debug = true;
		// @formatter:on
		for (int i = 0; i < data.length; i++) {
			StringWriter out = new StringWriter();
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV5 parser = new PolicyComponentParserV5(res, users, apps, locs, envs);
			parser.processPolicy(pol, data[i][1], data[i][0], data[i][2]);
			System.out.println("Processing Policy "+i+": " + data[i][0]);
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
	public void testResourceOnly() throws IOException {
		// @formatter:off
		String[][] data = {
				{
					"BY TRUE",
					"FOR (resource.fso.\"itar property\" = \"True\" AND resource.fso.ear = \"True\")",
					"",
					//result
					"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
					"<RESOURCE id=\"0\"><PROPERTY name=\"itar property\" method=\"EQ\" value=\"True\"/><PROPERTY name=\"ear\" method=\"EQ\" value=\"True\"/></RESOURCE>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
					"<xml-fragment/>",
				},
				{
				"BY TRUE",
				"FOR TRUE",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/><PROPERTY name=\"name5\" method=\"EQ\" value=\"val5\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/><PROPERTY name=\"name5\" method=\"EQ\" value=\"val5\"/><PROPERTY name=\"name6\" method=\"EQ\" value=\"val6\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/><PROPERTY name=\"name5\" method=\"EQ\" value=\"val5\"/><PROPERTY name=\"name6\" method=\"EQ\" value=\"val6\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"NE\" value=\"val1\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"NE\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"NE\" value=\"val2\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"NE\" value=\"val2\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			{
				"FOR (resource.fso.name1 = val1 OR ((resource.fso.name2 = val2) AND NOT((resource.fso.name3 = val3 AND resource.fso.name4 = val4))))",
				"BY TRUE",
				"",
				//result
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name4\" method=\"NE\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name4\" method=\"NE\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"NE\" value=\"val4\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name5\" method=\"NE\" value=\"val5\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"NE\" value=\"val4\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name5\" method=\"NE\" value=\"val5\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"NE\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/><PROPERTY name=\"name5\" method=\"EQ\" value=\"val5\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/><PROPERTY name=\"name3\" method=\"NE\" value=\"val3\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/><PROPERTY name=\"name5\" method=\"EQ\" value=\"val5\"/></RESOURCE></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
		};
//		debug = true;
		// @formatter:on
		for (int i = 0; i < data.length; i++) {
			POLICY pol = POLICY.Factory.newInstance();
			RESOURCES res = RESOURCES.Factory.newInstance();
			USERS users = USERS.Factory.newInstance();
			APPLICATIONS apps = APPLICATIONS.Factory.newInstance();
			LOCATIONS locs = LOCATIONS.Factory.newInstance();
			ENVS envs = ENVS.Factory.newInstance();
			PolicyComponentParserV5 parser = new PolicyComponentParserV5(res, users, apps, locs, envs);
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
				"<xml-fragment usergroup=\"0\"><CONDITION type=\"app\" exclude=\"false\">0</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
				"<APPLICATION id=\"0\"><PROPERTY name=\"prop\" method=\"EQ\" value=\"prop\"/><PROPERTY name=\"pro\" method=\"EQ\" value=\"pro\"/><PROPERTY name=\"blah1\" method=\"EQ\" value=\"blah1\"/><PROPERTY name=\"prop1\" method=\"EQ\" value=\"prop1\"/><PROPERTY name=\"prop2\" method=\"EQ\" value=\"prop2\"/><PROPERTY name=\"prop3\" method=\"EQ\" value=\"2.45AM\"/><PROPERTY name=\"prop4\" method=\"EQ\" value=\"2.45 AM\"/><PROPERTY name=\"blah\" method=\"EQ\" value=\"blah\"/></APPLICATION>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
			// this next case is failing because of quotation problems in the property name, e.g. resource.fso."name with spaces" = someVal
			{
				"FOR TRUE",
				"BY ((user.u1 = v1 AND user.name = nameVal AND \"user.loc with space\" <= \"San Mateo\") AND (application.n1 = V1 AND application.n2 > V2 AND application.n3 = V3 AND application.\"name with space\" != \"value with space\") AND (host.h1 = hh AND host.h2 = \"with space\"))",
				"",
				//result
				"<xml-fragment><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"app\" exclude=\"false\">0</CONDITION><CONDITION type=\"loc\" exclude=\"false\">0</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<USER id=\"0\"><PROPERTY name=\"u1\" method=\"EQ\" value=\"v1\"/><PROPERTY name=\"name\" method=\"EQ\" value=\"nameVal\"/><PROPERTY name=\"loc with space\" method=\"LE\" value=\"San Mateo\"/></USER>",
				"<APPLICATION id=\"0\"><PROPERTY name=\"n1\" method=\"EQ\" value=\"V1\"/><PROPERTY name=\"n2\" method=\"GT\" value=\"V2\"/><PROPERTY name=\"n3\" method=\"EQ\" value=\"V3\"/><PROPERTY name=\"name with space\" method=\"NE\" value=\"value with space\"/></APPLICATION>",
				"<LOCATION id=\"0\"><PROPERTY name=\"h1\" method=\"EQ\" value=\"hh\"/><PROPERTY name=\"h2\" method=\"EQ\" value=\"with space\"/></LOCATION>",
				"<xml-fragment/>",
			},
			{
				"FOR TRUE",
				"BY user.GROUP has 0 OR host.name = prince AND application.name = ACROBAT OR application.path = Drive",
				"",
				//result
				"<xml-fragment usergroup=\"0\"><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"app\" exclude=\"false\">0,1</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<USER id=\"0\"><PROPERTY name=\"name\" method=\"EQ\" value=\"prince\"/></USER>",
				"<xml-fragment><APPLICATION id=\"0\"><PROPERTY name=\"name\" method=\"EQ\" value=\"ACROBAT\"/></APPLICATION><APPLICATION id=\"1\"><PROPERTY name=\"path\" method=\"EQ\" value=\"Drive\"/></APPLICATION></xml-fragment>",
				"<xml-fragment/>",
				"<xml-fragment/>",
			},
            // this next case is failing although the problem may have been resolved already
			{
				"FOR TRUE",
				"BY user.GROUP has 0",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1,2,3</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/></RESOURCE><RESOURCE id=\"2\"><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/></RESOURCE><RESOURCE id=\"3\"><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"name\" method=\"EQ\" value=\"**.jt\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"name1\" method=\"EQ\" value=\"val1\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"name2\" method=\"EQ\" value=\"val2\"/><PROPERTY name=\"name3\" method=\"EQ\" value=\"val3\"/><PROPERTY name=\"name4\" method=\"EQ\" value=\"val4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property1\"/><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property2\"/><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property3\"/></RESOURCE>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0,1</CONDITION>",
				"<xml-fragment><RESOURCE id=\"0\"><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property1\"/><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property3\"/><PROPERTY name=\"comp4\" method=\"EQ\" value=\"property4\"/></RESOURCE><RESOURCE id=\"1\"><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property2\"/><PROPERTY name=\"comp3\" method=\"EQ\" value=\"property3\"/><PROPERTY name=\"comp4\" method=\"EQ\" value=\"property4\"/></RESOURCE></xml-fragment>",
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
				"<CONDITION type=\"res\" exclude=\"false\">0</CONDITION>",
				"<RESOURCE id=\"0\"><PROPERTY name=\"program\" method=\"EQ\" value=\"PR-01\"/><PROPERTY name=\"bafa\" method=\"EQ\" value=\"BAFA-01\"/></RESOURCE>",
				"<xml-fragment/>",
				"<xml-fragment/>", 
				"<xml-fragment/>", 
				"<xml-fragment/>", 
			},
            // this next case is broken but should be fixed in the latest verion of the code
			{
				"FOR TRUE",
				//"BY (user.GROUP has 2 AND ((user.locationcode = \"SG\" AND user.user_location_code = \"IN\") AND (user.dummyproperty1 = \"dummy\" AND user.dummyproperty2 = \"dummy2\")) AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\"))",
				"BY ((user.GROUP has 2 AND (user.locationcode = \"SG\" AND user.user_location_code = \"IN\")) AND (host.inet_address = \"10.63.1.144\" AND host.inet_address = \"10.63.0.101\"))",
				"",
				//result
				"<xml-fragment usergroup=\"2\"><CONDITION type=\"usr\" exclude=\"false\">0</CONDITION><CONDITION type=\"loc\" exclude=\"false\">0</CONDITION></xml-fragment>",
				"<xml-fragment/>",
				"<USER id=\"0\"><PROPERTY name=\"locationcode\" method=\"EQ\" value=\"SG\"/><PROPERTY name=\"user_location_code\" method=\"EQ\" value=\"IN\"/></USER>",
				"<xml-fragment/>", 
				"<LOCATION id=\"0\"><PROPERTY name=\"inet_address\" method=\"EQ\" value=\"10.63.1.144\"/><PROPERTY name=\"inet_address\" method=\"EQ\" value=\"10.63.0.101\"/></LOCATION>",
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
			PolicyComponentParserV5 parser = new PolicyComponentParserV5(res, users, apps, locs, envs);
			System.out.println("Processing record"+ (i + 1));
			parser.processPolicy(pol, data[i][0], data[i][1], data[i][2]);
//			debug = true;
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
		System.out.println("Done");
	}	
}
