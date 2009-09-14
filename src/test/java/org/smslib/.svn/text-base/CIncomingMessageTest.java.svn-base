/**
 * 
 */
package org.smslib;

import net.frontlinesms.junit.BaseTestCase;

/**
 * Tests messages created using {@link TpduUtils} against {@link CIncomingMessage}.
 * 
 * @author Alex
 *
 */
public class CIncomingMessageTest extends BaseTestCase {
	private final CIncomingMessageTest LOG = this;
	
	/** PDUs of UCS-2 encoded text messages containing some cyrillic characters. */
	private static final String[] UCS2_PDU = {
		"0791449737709399040C914477493184380008902050710511000C0420044D04410441049B0456",
		"0791447728008000040C914497885156050008902001412023001C042404440442043504440442043F044904350446044B0432043B0434",
		"0791449737709399040C91447749318438000890205071858300540420044D04410441049B0456000A06270628062A062C062E062F06310633000A00530065006E0074002000660072006F006D002000460072006F006E0074006C0069006E00650053004D00530020003B002D0029",
		"0791447728008000440C914497885156050008902021217274008C0500030E02010420044304340434045E0020044C043D00200430043A044804430442043200210020005400680061007400270073002000760065007200790020006500780063006900740069006E0067002C0020006C00650074002700730020006D0061006B0065002000740068006900730020006D0065007300730061006700650020006D0075006C0074",
		"0791447728008000440C91449788515605000890202121728400560500030E020200690020007000610072007400200061006E006400200063006F006E0063006100740065006E0061007400650064002000620079002000770072006900740069006E00670020006C006F00740073002E",

	};
	/** UCS-2 strings contained within {@link #UCS2_PDU} */
	private static final String[] UCS2_TEXT = {
		"Рэссқі",
		"Ффтефтпщецывлд",
		"Рэссқі\nابتجخدرس\nSent from FrontlineSMS ;-)",
		"Руддў ьн акшутв! That's very exciting, let's make this message mult",
		"i part and concatenated by writing lots.",
	};
	
	private static final String[] GSM_7BIT_PDU = {
		"0791447728008000040C9144978851560500009020113174630023C7F79B0C32BFE5A0FCBBEE024DD96138E8ED06D1D16590383C5E83CAF4B10B",
		/* N.B. Originator = "from_Orange"; address length=20; type byte = 0xD0 */"07914497370190370414D066F9BB1D79CAC3EE731900009020114181110097C834C82C7FB7414F79D87D2EBB40D4F0BA0CA2A3CB2074783E679741EF3A1DF43683E86F383CED3E83EA7017681866B341B41A0C447F83E4E5737A4E2FCB41F9775D0E2297C569FA6B2C2F93D374D0382C27835A203ABAEC06E5DF75D038EC06D1DFF0561D0E0ABBF3F474BB0C12E741F4329E0E7ACB41E23C681C66B3D3EE33885683B900",
		"0791447728008000040C914497885156050000902011515484003D8D6030B93A4FABC16030390C469BC7E0B9688EAE6765FADD8D534683C26034280C4683C260741C0C06E14F715EA816CB6274EDB50603",
		"0791449737709399040C91447749318438000090201161305400475474595E06A5E7A030C85DBE83CC6F791B14B687D3ECB0985D0699DF729051FE76D3D9697779DA9C828C6F797BAE030541F6B23C0F9AA3DF723AC8FC96B701",
		"0791449737019037040C91447749318438000090201171033000258D6030B942E7ABC16030390C469BC7E0B968D6B2B54461B07D544661C36030D803",
		"0791449737019037040C91448773276057000090201171124200198D6030B942E7ABC160B0380C469B6C6234190C067B3D",
		"0791449737019037640C91447749318438000090201171853100A0060804B9DB0201C8B2BC0C4ACF416190BD2CCF83EC65791E642FCBF3207B599E07D9CBF23CC85E96E741F6B23C0FB297E57990BD2CCF83EC65791E642FCBF3207B599E07D9CBF23CC85E96E741F6B23C0FB297E57990BD2CCF83EC65791EC47EBBCFA076793E0F9FCBA03B3A3D4683C2637A3DCC66E7417378D83D07D1EF6FD09B8E2ECB41EDF27C1E3E97E7",
		"0791449737019037640C9144774931843800009020117185630025060804B9DB0202A0303C2CA783CCF2771B444797416F79FA9C7687D9A0B7BB1C02",
		"0791449737709499440C91448773276057000080708031005340A005000347020189EFBA985D06B5CBF379F85C7681826E7A985D06B5CBF379F81C0691DF7531BB4C06B5CBF379F81C0685DDF430BB0C0ABBE9617619447FA7D9A0B09B0C9ABBEBE375590E7AB3C9A0F0B9CE9E83DA6C1B1DCD0699DF6CFA1BD466B7E96E503B4FBFD7F5F57B5D5FBFD7F5F57B5D5FBFD7F5F57B5D5FBFD7F5F57B5D5FBFD7F5F57B5D5FBFD7F5",
		
	};
	private static final String[] GSM_7BIT_TEXT = {
		"Good for you. Slap on the back etc.",
		"Hi from Orange. Take the hassle out of topping up. Call 450 to register your debit/credit card - then you can top-up anytime by text or by calling 450.",
		"\rAAI+gSUAAAICAQMGAgEfQk3etwn8JQABAQABAQABAQcAAApObyBjb21tZW50",
		"There is a new form available for FrontlineSMS Forms: A very short form",
		"\rAAI+hyUAAAICAQMGAgEfZlZDBAmGJQ0CAAA=",
		"\rAAI+hyUAAAECAQMlDQIAAA==",
		"Here is a very very very very very very very very very very very very very very very very very very long message which actually spans two other messages",
		" apart from the original one!",
		"Double message. Antale messaga doubled messaga antale antale toil and snucker old aguls ml6thl folto mlmtn mytwuzuwuzuwuzuwuzuwuzuwuzuwuzuwuzuwuzuwuzuwuz",
	};
	
	private static final String[] BINARY_PDU = {
		"0791449737019037640C91447749318438000490201171434400430605043E870000012500000025000101000103070A00114120766572792073686F727420666F726D0F0800010010436F6D6D656E74732C20706C656173650A09000105",
	};
	private static final byte[][] BINARY_BINARY = {
		{1, 37, 0, 0, 0, 37, 0, 1, 1, 0, 1, 3, 7, 10, 0, 17, 65, 32, 118, 101, 114, 121, 32, 115, 104, 111, 114, 116, 32, 102, 111, 114, 109, 15, 8, 0, 1, 0, 16, 67, 111, 109, 109, 101, 110, 116, 115, 44, 32, 112, 108, 101, 97, 115, 101, 10, 9, 0, 1, 5},
	};
	
	public void testbad7bitGsmMessages() throws Throwable {
		String pdu = GSM_7BIT_PDU[0].substring(0, GSM_7BIT_PDU[0].length()-2);
		try {
			new CIncomingMessage(pdu, 0, "");
			fail("Expected MessageDecodeException.");
		} catch(MessageDecodeException ex) {}
	}
	
	public void test7bitGsmMessages() throws Throwable {
		for (int i = 0; i < GSM_7BIT_PDU.length; i++) {
			String pdu = GSM_7BIT_PDU[i];
			String expectedText = GSM_7BIT_TEXT[i];
			LOG.info("Testing message " + i + "'"+expectedText+"'");
			CIncomingMessage message = new CIncomingMessage(pdu, 0, "");
			assertEquals("GSM 7-bit Message not decoded by old implementation.", expectedText, message.getText());
		}
	}
	
	/**
	 * Tests unicode messages on the new implementation.
	 * @throws Throwable 
	 */
	public void testUnicodeMessages_newImplementation() throws Throwable {
		// Test new implementation alone.
		for (int i = 0; i < UCS2_PDU.length; i++) {
			String pdu = UCS2_PDU[i];
			String expectedText = UCS2_TEXT[i];
			CIncomingMessage newImplementation = new CIncomingMessage(pdu, 0, null);
			assertEquals("Simple UCS-2 Message not decoded by new implementation.", expectedText, newImplementation.getText());
		}
	}
	
	/**
	 * Test some unicode messages that have been slightly mangled.
	 * @throws Throwable
	 */
	public void testBadUnicodeMessages() throws Throwable {
		String badHexLength = UCS2_PDU[0].substring(0, UCS2_PDU[0].length()-1);
		try {
			new CIncomingMessage(badHexLength, 0, null);
			fail("Expected MessageDecodeException");
		} catch(MessageDecodeException ex) {}

		String badStart2 = UCS2_PDU[0].substring(2);
		try {
			new CIncomingMessage(badStart2, 0, null);
			fail("Expected MessageDecodeException");
		} catch(MessageDecodeException ex) {}
	}
	
	public void test8bitMessages() throws Throwable {
		for (int i = 0; i < BINARY_PDU.length; i++) {
			String pdu = BINARY_PDU[i];
			byte[] expectedBinary = BINARY_BINARY[i];
			CIncomingMessage message = new CIncomingMessage(pdu, 0, "");
			assertEquals("Binary message not decoded by new implementation.", expectedBinary, message.getBinary());
		}
	}

	private void info(String message) {
		System.out.println("INFO: " + message);
	}
	
	public void testTest() throws Throwable {
		String pdu = "0791449737709499440C9144877327605700008060904185124098060804B0930301B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562B1582C168BC562";
		new CIncomingMessage(pdu, 0, null);
	}
}