package net.frontlinesms.hex;

import net.frontlinesms.junit.BaseTestCase;

public class HexUtilsTest extends BaseTestCase {
	private static final TestData[] VALID = {
		new TestData("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F606162636465666768696A6B6C6D6E6F707172737475767778797A7B7C7D7E7F808182838485868788898A8B8C8D8E8F909192939495969798999A9B9C9D9E9FA0A1A2A3A4A5A6A7A8A9AAABACADAEAFB0B1B2B3B4B5B6B7B8B9BABBBCBDBEBFC0C1C2C3C4C5C6C7C8C9CACBCCCDCECFD0D1D2D3D4D5D6D7D8D9DADBDCDDDEDFE0E1E2E3E4E5E6E7E8E9EAEBECEDEEEFF0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF",
				new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,(byte)128,(byte)129,(byte)130,(byte)131,(byte)132,(byte)133,(byte)134,(byte)135,(byte)136,(byte)137,(byte)138,(byte)139,(byte)140,(byte)141,(byte)142,(byte)143,(byte)144,(byte)145,(byte)146,(byte)147,(byte)148,(byte)149,(byte)150,(byte)151,(byte)152,(byte)153,(byte)154,(byte)155,(byte)156,(byte)157,(byte)158,(byte)159,(byte)160,(byte)161,(byte)162,(byte)163,(byte)164,(byte)165,(byte)166,(byte)167,(byte)168,(byte)169,(byte)170,(byte)171,(byte)172,(byte)173,(byte)174,(byte)175,(byte)176,(byte)177,(byte)178,(byte)179,(byte)180,(byte)181,(byte)182,(byte)183,(byte)184,(byte)185,(byte)186,(byte)187,(byte)188,(byte)189,(byte)190,(byte)191,(byte)192,(byte)193,(byte)194,(byte)195,(byte)196,(byte)197,(byte)198,(byte)199,(byte)200,(byte)201,(byte)202,(byte)203,(byte)204,(byte)205,(byte)206,(byte)207,(byte)208,(byte)209,(byte)210,(byte)211,(byte)212,(byte)213,(byte)214,(byte)215,(byte)216,(byte)217,(byte)218,(byte)219,(byte)220,(byte)221,(byte)222,(byte)223,(byte)224,(byte)225,(byte)226,(byte)227,(byte)228,(byte)229,(byte)230,(byte)231,(byte)232,(byte)233,(byte)234,(byte)235,(byte)236,(byte)237,(byte)238,(byte)239,(byte)240,(byte)241,(byte)242,(byte)243,(byte)244,(byte)245,(byte)246,(byte)247,(byte)248,(byte)249,(byte)250,(byte)251,(byte)252,(byte)253,(byte)254,(byte)255}),
//		new TestData(),
//		new TestData(),
//		new TestData(),
//		new TestData(),
	};


	public void testEncode() {
		for(TestData data : VALID) {
			assertEquals("data encoded incorrectly.", data.hex, HexUtils.encode(data.bytes));
		}
	}

	public void testCodec() {
		for(TestData data : VALID) {
			assertEquals("data decen incorrectly.", data.bytes, HexUtils.decode(HexUtils.encode(data.bytes)));
			assertEquals("data endec incorrectly.", data.hex, HexUtils.encode(HexUtils.decode(data.hex)));
		}
	}
	
	public static void main(String[] args) {
		byte[] bytes = new byte[256];
		for (int i = 0; i <= 255; i++) {
			System.out.println(i);
			bytes[i] = (byte)i;
		}
		System.out.println(HexUtils.encode(bytes));
	}
	
	public void testDecode() {
		// test odd length
		try {
			HexUtils.decode("A");
			fail("Odd length hex string not valid.");
		} catch(HexDecodeException ex) {}
		try {
			HexUtils.decode("012345678");
			fail("Odd length hex string not valid.");
		} catch(HexDecodeException ex) {}
		
		// test invalid characters
		testInvalidCharacters("0123456789ABCDEFGH");
		testInvalidCharacters(new String(new byte[]{0,1,2,3,4,5,6,7}));
		
		// test valid
		for(TestData data : VALID) {
			assertEquals("Data decoded incorrectly", data.bytes, HexUtils.decode(data.hex));
		}
	}
	
	private static void testInvalidCharacters(String input) {
		try {
			HexUtils.decode(input);
			fail("Should have failed for invalid characters in:" + input);
		} catch(HexDecodeException ex) {}
	}
	
	private static class TestData {
		private final String hex;
		private final byte[] bytes;
		
		TestData(String hex, byte[] bytes) { 
			this.hex = hex;
			this.bytes = bytes;
		}
	}
}
