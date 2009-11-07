/**
 * 
 */
package org.smslib.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Random;

import org.smslib.CIncomingMessage;
import org.smslib.MessageDecodeException;
import org.smslib.sms.PduInputStream;
import org.smslib.sms.SmsMessageEncoding;

import net.frontlinesms.junit.BaseTestCase;

/**
 * Tests the UD encoding of {@link TpduUtils} against the UD decoding of {@link TpduUtils}.
 * 
 * @author Alex
 */
public class TpduUtilsTest extends BaseTestCase {
	
//> Message constants.  These are mostly for convenience.
	private static final String[] TEXTS = {
		"",
		"Hello, here is a short test message.",
		"Here is an sms text message that contains precisely one hundred and sixty characters, and should therefore fit (un)comfortably inside a single SMS text message.",
		"Here is some more text.",
		"Here is my latest in a long line of very long messages along the line of creating a set of short message service messages that describe a single text message to be sent to myself in order to test my software's messaging capabilities.",
		"This proposition should not sound great. If you think you're going to save time in the long run by throwing your data into a big bucket now, then sifting through it later.",
		"And they're clearly inspired by a certain famous fruity phone: witness the positioning round menu button and earpiece slit, note the shiny black faceplate and touch-sensitive screen.",
		"Most of the HBOS losses are blamed on a £7bn write-down at its corporate division, which is heavily exposed to the housing and commercial property sectors. In 2007, HBOS made a profit of £5.7bn.",
		"There is a new form available for FrontlineSMS Forms: A very short form",
		//,
	};
	
	private static final short[] PORTS = {
		0,
		100, 16000
	};
	
	private static final String[] MSISDNS = {
		"07890246735",
		"+447890123456"
	};
	
	public static final int[] MP_REF_NO = {0, 55, 255};
	/**
	 * SMS Lib originally only supported international SMSC numbers, so that is what we provide here.
	 */
	public static final String[] SMSC_NUMBER = {"", "+441234567890"};


	/** UCS-2 strings contained within {@link #UCS2_PDU} */
	private static final String[] UCS2_TEXT = {
		"",
		"Рэссﾛі",
		"Ффтефтпщецывлд",
		"Рэссﾛі\nابتجخدرس\nSent from FrontlineSMS ;-)",
		"Руддў ьн акшутв! That's very exciting, let's make this message mult",
		"i part and concatenated by writing lots.",
		"Җ First, there is a Cyrillic character to force us!",
		"Җ First, there is a Cyrillic character to force us into UCS-2 mode.  After that, we just continue in English as normal!",
	};
	
	/** Test UCS-2 message encoding and decoding using randomly generated strings. */
	public void testUcs2_generated() throws MessageDecodeException {
		// 8 bit messages
		Random random = new Random(0);
		for (int j = 0; j < 320; j++) {
			char[] charz = new char[Math.abs(random.nextInt(180))];
			for(int i=0; i<charz.length; ++i) {
				charz[i] = (char)random.nextInt();
			}
			String messageText = new String(charz);
			if(log.isTraceEnabled()) log.trace(messageText);
			for(short sourcePort : PORTS) {
				for(short destinationPort : PORTS) {
					for(String recipient : MSISDNS) {
						for(int mpRefNo : MP_REF_NO) {
							assertEquals("UCS2 codecs do not agree.", messageText, TpduUtils.decodeUcs2Text(TpduUtils.encodeUcs2Text(messageText)));
							
							String[] pdus = TpduUtils.generatePdus_ucs2(messageText, "", recipient, mpRefNo, sourcePort, destinationPort, true, 0, 0, TpduUtils.getDcsByte(SmsMessageEncoding.UCS2));
							
							String rebuiltText = "";
							for(String pdu : pdus) {
								CIncomingMessage message = new CIncomingMessage(convertOutgoingToIncoming(pdu), 0, "");
								rebuiltText += message.getText();
								assertEquals("", sourcePort, message.getSourcePort());
								assertEquals("", destinationPort, message.getDestinationPort());
								if(pdus.length > 1) {
									// Only check MP REF NO if we have a multipart message.
									assertEquals("", mpRefNo, message.getMpRefNo());
								}
							}
							assertEquals("", messageText, rebuiltText);
						}
					}
				}
			}
		}
	}
	
	public void testUcs2_strings() throws MessageDecodeException {
		for(String messageText : UCS2_TEXT) {
			log.trace("messageText: '"+messageText+"'");
			for(short sourcePort : PORTS) {
				for(short destinationPort : PORTS) {
					for(String recipient : MSISDNS) {
						for(int mpRefNo : MP_REF_NO) {
							assertEquals("UCS2 codecs do not agree.", messageText, TpduUtils.decodeUcs2Text(TpduUtils.encodeUcs2Text(messageText)));
							
							String[] pdus = TpduUtils.generatePdus_ucs2(messageText, "", recipient, mpRefNo, sourcePort, destinationPort, true, 0, 0, TpduUtils.getDcsByte(SmsMessageEncoding.UCS2));
							
							String rebuiltText = "";
							for(String pdu : pdus) {
								CIncomingMessage message = new CIncomingMessage(convertOutgoingToIncoming(pdu), 0, "");
								rebuiltText += message.getText();
								assertEquals("", sourcePort, message.getSourcePort());
								assertEquals("", destinationPort, message.getDestinationPort());
								if(pdus.length > 1) {
									// Only check MP REF NO if we have a multipart message.
									assertEquals("", mpRefNo, message.getMpRefNo());
								}
							}
							assertEquals("", messageText, rebuiltText);
						}
					}
				}
			}
		}
	}
	
	public void testSplitTextUcs2() {
		assertEquals("Empty string should split into one message part (not ported).", 1, TpduUtils.splitText_ucs2("", false).length);
		assertEquals("Empty string should split into one message part (ported).", 1, TpduUtils.splitText_ucs2("", true).length);
		
		final boolean[] bools = new boolean[]{true, false};
		for(boolean isPorted : bools) {
			for(String messageText : UCS2_TEXT) {
				String[] messageParts = TpduUtils.splitText_ucs2(messageText, isPorted);
				System.out.println("'"+messageText+"'");
				for (int i = 0; i < messageParts.length; i++) {
					System.out.println(i + ": " + messageParts[i]);
				}
				System.out.println("---");
			}
		}
	}
	
	/**
	 * Tests the method {@link TpduUtils#getPayloads(byte[], int, int)} to make sure that
	 * parts generated are the correct length and have the correct content.
	 * @throws IOException 
	 */
	public void testSplit8bit() throws IOException {
		assertEquals("Splitting no data should get 1 empty part (unported).", 1, TpduUtils.getPayloads(new byte[0], 0, 0).length);
		assertEquals("Splitting no data should get 1 empty part (ported).", 1, TpduUtils.getPayloads(new byte[0], 1, 1).length);
		
		Random randy = new Random(0);
		
		// check single part messages are NOT split
		for(int sourcePort : PORTS) {
			for(int destinationPort : PORTS) {
				boolean isPorted = sourcePort != 0 || destinationPort != 0;
				int udhLengthSinglePartMessage = (isPorted ? TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT_LENGTH+2/*IEI type+length*/+1/*UDH-Length*/ : 0);
				int maxPayloadSingleMessage = TpduUtils.MAX_PDU_SIZE - udhLengthSinglePartMessage;
				
				for(int payloadLength = 0; payloadLength <= maxPayloadSingleMessage; ++payloadLength) {
					byte[] testMessage = new byte[payloadLength];
					randy.nextBytes(testMessage);
					byte[][] payloadParts = TpduUtils.getPayloads(testMessage, sourcePort, destinationPort);
					assertEquals("Too many message parts for binary message of length: " + payloadLength + " octets", 1, payloadParts.length);
					assertEquals("Message content incorrect.", testMessage, payloadParts[0]);
				}
			}
		}
		
		// check two part messages ARE split
		for(int sourcePort : PORTS) {
			for(int destinationPort : PORTS) {
				boolean isPorted = sourcePort != 0 || destinationPort != 0;
				int udhLengthSinglePartMessage = (isPorted ? TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT_LENGTH+2+1 : 0);
				assertEquals("Test UDH length is incorrect.", TpduUtils.getUDHSize(true, isPorted, false), udhLengthSinglePartMessage);
				int maxPayloadSingleMessage = TpduUtils.MAX_PDU_SIZE - udhLengthSinglePartMessage;
				int udhLengthMultipartMessage = 1
						+ (isPorted ? TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT_LENGTH+2 : 0)
						+ 2 + (TpduUtils.CONCAT_USE_16_BIT ? TpduUtils.TP_UDH_IEI_APP_PORTING_16BIT_LENGTH : TpduUtils.TP_UDH_IEI_CONCAT_SMS_8BIT_LENGTH);
				assertEquals("Test UDH length is incorrect.", TpduUtils.getUDHSize(true, isPorted, true), udhLengthMultipartMessage);
				int maxPayloadMultipartMessage = TpduUtils.MAX_PDU_SIZE - udhLengthMultipartMessage;
				
				for(int payloadLength = maxPayloadSingleMessage+1; payloadLength <= maxPayloadMultipartMessage*2; ++payloadLength) {
					byte[] testMessage = new byte[payloadLength];
					randy.nextBytes(testMessage);
					byte[][] payloadParts = TpduUtils.getPayloads(testMessage, sourcePort, destinationPort);
					assertEquals("Too many message parts for binary message of length: " + payloadLength + " octets", 2, payloadParts.length);
					
					ByteArrayOutputStream messageRebuild = new ByteArrayOutputStream();
					for (int j = 0; j < payloadParts.length; j++) {
						byte[] payloadPart = payloadParts[j];
						assertTrue("Payload part " + j + " is too large (" + (payloadPart.length + udhLengthMultipartMessage) + " > " + TpduUtils.MAX_PDU_SIZE + ")", payloadPart.length + udhLengthMultipartMessage <= TpduUtils.MAX_PDU_SIZE);
						messageRebuild.write(payloadPart);
					}
					assertEquals("Message content incorrect.", testMessage, messageRebuild.toByteArray());
				}
			}
		}
	}
	
	
	public void test8bit() throws MessageDecodeException {
		// 8 bit messages
		Random random = new Random(0);
		for (int j = 0; j < 320; j++) {
			byte[] bytes = new byte[Math.abs(random.nextInt(400))];
			random.nextBytes(bytes);
			for(short sourcePort : PORTS) {
				for(short destinationPort : PORTS) {
					for(String recipient : MSISDNS) {
						for(int mpRefNo : MP_REF_NO) {
							String[] pdus = TpduUtils.generatePdus_8bit(bytes, "", recipient, mpRefNo, sourcePort, destinationPort, true, 0, 0, TpduUtils.getDcsByte(SmsMessageEncoding.BINARY_8BIT));
							String rebuiltHex = "";
							for(String pdu : pdus) {
								CIncomingMessage message = new CIncomingMessage(convertOutgoingToIncoming(pdu), 0, "");
								rebuiltHex += HexUtils.encode(message.getBinary());
								assertEquals("", sourcePort, message.getSourcePort());
								assertEquals("", destinationPort, message.getDestinationPort());
								if(pdus.length > 1) {
									// Only check MP REF NO if we have a multipart message.
									assertEquals("", mpRefNo, message.getMpRefNo());
								}
							}
							assertEquals("", bytes, HexUtils.decode(rebuiltHex));
						}
					}
				}
			}
		}
	}
	
	public void test7bit() throws MessageDecodeException {
		int i = 0;

		// 7 bit messages
		for(String messageText : TEXTS) {
			for(short sourcePort : PORTS) {
				for(short destinationPort : PORTS) {
					for(String recipient : MSISDNS) {
						for(int mpRefNo : MP_REF_NO) {
							String[] pdus = TpduUtils.generatePdus_gsm7bit(messageText, "", recipient, mpRefNo, sourcePort, destinationPort, true, -1, 0, TpduUtils.getDcsByte(SmsMessageEncoding.GSM_7BIT));
							String rebuiltText = "";
							if(log.isTraceEnabled()) {
								log.trace("Expected text: " + messageText);
								log.trace("Source port: " + sourcePort);
								log.trace("Destination port: " + destinationPort);
								log.trace("Has UDH: " + (destinationPort!=0||sourcePort!=0||pdus.length>1));
							}
							for(String pdu : pdus) {
								CIncomingMessage message = new CIncomingMessage(convertOutgoingToIncoming(pdu), 0, "");
								rebuiltText += message.getText();
								assertEquals("", sourcePort, message.getSourcePort());
								assertEquals("", destinationPort, message.getDestinationPort());
								if(pdus.length > 1) {
									// Only check MP REF NO if we have a multipart message.
									assertEquals("", mpRefNo, message.getMpRefNo());
								}
								if(log.isTraceEnabled()) log.trace(++i);
							}
							assertEquals("", messageText, rebuiltText);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Strip the outgoing info off the front of a PDU and replace it with incoming info.
	 * @param pdu
	 * @return
	 */
	private String convertOutgoingToIncoming(String pdu) {
		if(log.isTraceEnabled()) {
			log.trace("TpduUtilsTest.convertOutgoingToIncoming()");
			log.trace("\tOUTGOING:\t" + pdu);
		}
		try {
			int byteZero;
			int pid;
			int dcs;
			byte[] userData;
			{
				PduInputStream in = new PduInputStream(pdu);
				
				TpduUtils.decodeMsisdnFromAddressField(in, true);
				
				// get the front byte, which identifies message content
				byteZero = in.read();
				
				// Message reference.  Always zero here. 
				/** [TP-MR: TP-Message-Reference] Parameter identifying the SMS-SUBMIT. */ 
				int messageReference = in.read();
		
				// Add the recipient's MSISDN
				/** [TP-DA: TP-Destination-Address] Address of the destination SME. */
				{
					int length = in.read();
					int byte0 = in.read();
					String s = "";
					for(int i=0; i<((length+1)>>1); ++i) s += in.read();
				}
				
				/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
				pid = in.read();
		
				/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
				dcs = in.read();
		
				/**
				 * [TP-VP: TP-Validity-Period] Parameter identifying the time from where the message is no longer valid.
				 * Here, this is always relative.
				 */
				in.read();
		
				// Transfer the UD to a byte array
				{
					ByteArrayOutputStream ud = new ByteArrayOutputStream();
					int read;
					try {
						while((read = in.read()) != -1) ud.write(read);
					} catch(EOFException ex) { /* We've reached the end of the Stream. */ }
					userData = ud.toByteArray();
				}
			}
			// Now we've removed all the outgiong-specific info, we need to add the incoming specific info.
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			{
				// TODO is this the smsc number???
				out.write(TpduUtils.encodeMsisdnAsAddressField("+447890123456", true));
				
				// get the front byte, which identifies message content
				out.write(byteZero); // TODO this byteZero is not suitable for an MT message as Message-Type-Indicator is MO-specific
				boolean hasUdh = (byteZero & TpduUtils.TP_UDHI) != 0;
		
				/** [TP-OA: TP-Originating-Address] Address of the originating SME. */
				out.write(TpduUtils.encodeMsisdnAsAddressField("+447988156555", false));
		
				/** [TP-PID: TP-Protocol-Identifier] Parameter identifying the above layer protocol, if any. */
				out.write(pid);
		
				/** [TP-DCS: TP-Data-CodingScheme] Parameter identifying the coding scheme within the TP-User-Data. */
				out.write(dcs);
				
				/** [TP-SCTS: TP-Service-Centre-Time-Stamp] Parameter identifying time when the SC received the message. */
				for(int i=0; i<7; ++i) out.write(0);
			}
			
			out.write(userData);
			String incomingPdu = HexUtils.encode(out.toByteArray());
			if(log.isTraceEnabled()) {
				log.trace("\tINCOMING:\t" + incomingPdu);
				log.trace("TpduUtilsTest.convertOutgoingToIncoming()");
			}
			return incomingPdu;
		} catch(IOException ex) {
			return null;
		}
	}
	
	/** These are messages that users of FrontlineSMS have submitted, which previously broke the code. */
	private static final String[] SUBMITTED_PDUS = {
		"07913366003000F0240B913366816205F30000903001610065400454329C0E",
		"07913386094000F0240B913386143077F7000090301161035340014A",
		"069142831130F3040C914283910294880011903031215091009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F3040C914283316159513F119030612193510025C230BB4E0F83A061BA3C3D5EB39669F71C1D9E875BCDB79B0E729EC3E63A3BEC02", // T.A. 20090317
		"069142831130F2040C914283415898740011903051815523005DCE775B470C5A9344100804028140201008FA9ED3DDEFB66E598506412010A8DC0EA7D93A62D09A240693CD228099B4165D462988B5AC4283C164B308C80691CFA7CB2805B19CD5A60E868BD170355C8E7603", // T.A. 20090317
		"069142831130F2040C914283316159513F119030612193420025C230BB4E0F83A061BA3C3D5EB39669F71C1D9E875BCDB79B0E729EC3E63A3BEC02", // T.A. 20090317
		"069142831130F3040C914283811858270011903061916161007243D0981DAE93CB2072195E4FCF41EC30C89E66B3CB207219B44EBBE7E8F03CCC2287DD73103B0C1ABFDBEDBABB0C229741EB745B1C76CFCBEBB2ABF96E83C26479793E2F83CAAD7638CD068D4FE5395D376687EBE4729D1D66D7D761403E8C7EBF5D6639", // T.A. 20090317
		"069142831130F3040C914283910294880011903031213021009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F2040C914283512294997F11903041904511004DF3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FD6E87D36CD7F8DD06", // T.A. 20090317
		"069142831130F3040C914283910294880011903031213004009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F2040C914283910294880011903031215083009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F2040C914283316159513F119030419064920024C230BB4E0F83A061BA3C3D5EB39669F71C1D9E8759CDB79B0E729EC3E63A3B0C", // T.A. 20090317
		"069142831130F2040C914283512294997F11903041010042004DF3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FD6E87D36CD7F8DD06", // T.A. 20090317
		"069142831130F3040C914283012939860011903011215344004ECDB27C0C5240DF7539C8FEA6CB41EDF27C1E3E9741E1F1B80CA7974165375D5E0091C3EE39E82C7FD7E1207219240FC341653A887C2AD3E56590F92D6F17406537481E8603", // T.A. 20090317
		"069142831130F3040C9142830129398600119030112175110035C3F63B0D2ABBC76F39E86C07D9DFF539889C9683E2F532E8DC5630CB2079180E1ABFDBA0731BD40E83C66F78DA5D06", // T.A. 20090317
		"069142831130F3040C914283910294880011903031215065009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F2040C914283512294997F11903051216412004ED3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FDA6B7C369B66BFC6E03", // T.A. 20090317
		"069142831130F3040C914283512294997F11903041909585004DF3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FD6E87D36CD7F8DD06", // T.A. 20090317
		"069142831130F2040C914283017888487F08903051907531008400430020005600D40054005200200054004C00530050004300540041005400450052002000470041004200590020004700200056004F0055004C004500200032004D0041004E004400450020004C002000500052004F004700520041004D0020004400200056004F00540052002000530045005200490045002000520051004C0056002E", // T.A. 20090317
		"069142831130F3040C914283316159513F119030519042700025C230BB4E0F83A061BA3C3D5EB39669F71C1D9E875BCDB79B0E729EC3E63A3BEC02", // T.A. 20090317
		"069142831130F2040C914283017658133F119030319071630052E2B75BFDAECB41E33288FEAEABDF75F91C94B697DDA739280C6A87E7697718040FCFC76136A8FD7683DCF57659FE0691CB207A995D86A3DFEE32685C669741B05CAE66B3C564351B", // T.A. 20090317
		"069142831130F3040C9142830129398600119030112115730054C3F63B0D3AA7DFF6B0DB9D06298EB276D84D2E83DE0650794E6F81E2F532C8FDAECF416136FBED9E2BA665903C3C7EBBE9F2B21CF4AE83E66590FD9D9683CA7450BC5E6683D4EFBADC05", // T.A. 20090317
		"069142831130F3040C914283910294880011903031216012009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F2040881711107694100903041513402009C31DA0B367BC960B01C2856D3D1602E90F55D9F83C2F6B21E1403B9DF757B395C07B5CBF379F85C06D9DFE330DB05629641E4B2DC9D2ECB41EDF27C1E3E974170F9DB9E2EBBE9207219B492D166B898ED6693D9683217C85AAEA7D9ECB21E347EB7E1EF79590E62974131590C047FD7E52076790E1ABFDDF33A9B5E9683CEF230BD9EA697DB6537DD05", // T.A. 20090317
		"069142831130F3040C91428301805820001190300151851400514DF17BFD66BF59A0B01DE426A7C3EE73D94D0E83DC6F100E347E81D6E979D93D7F83D42750783C2EC3E96510B96C2FBBD37250BBDC16CBCB20FB9B2E2F8340E6B01B3466D7C52E", // T.A. 20090317
		"069142831130F3040C9142831129503900119030511201510046CB20F3E9542641CD2AB3DA140641D4E4101964819669F71C1D9E8741E377BB5D779741E432689D9E97DDF3378B9E1EA3C3F4F4B92C07E4C3E8F7DB659603", // T.A. 20090317
		"069142831130F2040C9142830129398600119030317030800039C2B75B2D078DDBEF34C8FDAEDB15E6B01B3466D7C52090BA0CB2BFEB73C5FC5D4787D3F43248FC76BBCB8A6BB9BC0695DD64", // T.A. 20090317
		"069142831130F2040C914283316159513F119030419034440024C230BB4E0F83A061BA3C3D5EB39669F71C1D9E8759CDB79B0E729EC3E63A3B0C", // T.A. 20090317
		"069142831130F2040C914283515157470011903061608383002E4D719D1DCEA741E377BC1EA6CBD378D63AED9EA3C3F3B00B347EC7EB61BA3C8D07E4C3E8F7DB659603", // T.A. 20090317
		"069142831130F2040C914283512294997F11903041909554004DF3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FD6E87D36CD7F8DD06", // T.A. 20090317
		"069142831130F2040C914283017658133F119030319022140052E2B75BFDAECB41E33288FEAEABDF75F91C94B697DDA739280C6A87E7697718040FCFC76136A8FD7683DCF57659FE0691CB207A995D86A3DFEE32685C669741B05CAE66B3C564351B", // T.A. 20090317
		"069142831130F2040C9142831167473100119030612074810028C277FBED4EB3826E729E357C09C3F27A5B5C6709D96137392F7E8B01F930FAFD7699E5", // T.A. 20090317
		"069142831130F3040C9142833166372500119030618175620027CE6713042A4E9FCD6410D402099FCBE0F2096A81884962120482E162339BED36ABC900", // T.A. 20090317
		"069142831130F3040C9142830176581320119030419090940034C2B75BFDAECB41E33288FEAEABDF75F91C94B697DDA739A8FD7683EAEDB2FC0D1A97D965102C97ABD96C3159CD06", // T.A. 20090317
		"069142831130F2040C914283012939860011903011310014004BC3F63B0D3AA7DFF6B0DB9D561CED20757D4E2F83ECEFFA1C340FB3EB653988FEAECF41653A485E6E97E5E37401047FD7E520FB9B2E07BDE5E7B03B3D0FD3D36FB70B", // T.A. 20090317
		"069142831130F2040C9142830129398600119030317070420031C3F63B0D529741F6779D5D00D9DFF53908344F83ECEFFA1C14B687D37410BB3C078DDFEE79594E9F2B1420", // T.A. 20090317
		"069142831130F3040C914283512294997F11903051216435004ED3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FDA6B7C369B66BFC6E03", // T.A. 20090317
		"069142831130F2040C914283316159513F119030412171150025C230BB4E0F83A061BA3C3D5EB39669F71C1D9E875BCDB79B0E729EC3E63A3BEC02", // T.A. 20090317
		"069142831130F3040C914283512294997F11903041010011004DF3705B1C06C1D9617ADA9D06ADD3EE393A3C0F83DCE7303B5D6E87417076989E76A7DBE1B6B80EC887D1EFB7CB2C07C1D9617ADA9D9E87DBE23000FD6E87D36CD7F8DD06", // T.A. 20090317
		"069142831130F2040C914283910294880011903031214065009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
		"069142831130F3040C9142838156878100119030110121200004D4F29C0E", // T.A. 20090317
		"069142831130F3040C914283017658133F119030319012300052E2B75BFDAECB41E33288FEAEABDF75F91C94B697DDA739280C6A87E7697718040FCFC76136A8FD7683DCF57659FE0691CB207A995D86A3DFEE32685C669741B05CAE66B3C564351B", // T.A. 20090317
		"069142831130F2040C914283017658133F119030319002450052E2B75BFDAECB41E33288FEAEABDF75F91C94B697DDA739280C6A87E7697718040FCFC76136A8FD7683DCF57659FE0691CB207A995D86A3DFEE32685C669741B05CAE66B3C564351B", // T.A. 20090317
		"069142831130F3040C9142831138491400119030610210630033CA3A394D4783D6757A7A1D66ADD3EE393A3C0FB3D8E976995E66A9EBE4341DBDAED3D3EB30201F46BFDF2EB31C", // T.A. 20090317
		"069142831130F2040C914283316159513F119030519052500025C230BB4E0F83A061BA3C3D5EB39669F71C1D9E875BCDB79B0E729EC3E63A3BEC02", // T.A. 20090317
		"069142831130F3040C914283910294880011903031216093009643B43C3DA7A7C36E500B442FBBCF6110689D76CFD1E179180462A6DB657AE1350FB3DFEEF31B040ADB5DA0F1DB3D2FA7D9207219644FB3D96590FB0492E56C3410686896D1CBEE7318045AC968335C2C2783D172381C083446CBD3737A3AEC8E98D3637419900FA3DF6F97590E02DDEF77D7D82CA397DDE7B06BBECE8BD9EFB36BFC6E03", // T.A. 20090317
	};

	/**
	 * Test messages that have been declared undecipherable and submitted in logs.
	 * @throws Throwable
	 */
	public void testUsersMessage() {
		for(String pdu : SUBMITTED_PDUS) {
			try {
				new CIncomingMessage(pdu, 0, "");
				System.out.println("Decoded ok: " + pdu);
			} catch (MessageDecodeException ex) {
				ex.printStackTrace();
				log.error("There was a problem decoding the PDU: " + pdu, ex);
				fail("There was a problem decoding the PDU (' " + ex.getMessage() + " '): " + pdu);
			}
		}
	}
	
	/**
	 * Tests our implementations of {@link TpduUtils#encodeMsisdnAsAddressField(String, boolean)} and
	 * {@link TpduUtils#decodeMsisdnFromAddressField(java.io.InputStream, boolean)} against each other.
	 * @throws Throwable 
	 */
	public void testSemiOctetEncoding() throws Throwable {
		String ukNumber = "+447890123456";
		testSemiOctetEncoding(ukNumber, true);
		testSemiOctetEncoding(ukNumber, false);
		String localUkNumber = "07890123456";
		testSemiOctetEncoding(localUkNumber, true);
		testSemiOctetEncoding(localUkNumber, false);
		
		Random randy = new Random(0);
		String phoneNumber = "";
		for(int i=0; i<15; ++i) {
			// Add a random digit to the phone number
			phoneNumber += (char)(randy.nextInt(10) + '0');
			testSemiOctetEncoding(phoneNumber, true);
			testSemiOctetEncoding(phoneNumber, false);
			testSemiOctetEncoding("+" + phoneNumber, true);
			testSemiOctetEncoding("+" + phoneNumber, false);
		}
	}
	
	private void testSemiOctetEncoding(String address, boolean isSmscNumber) throws IOException {
		byte[] encodedMsisdn = TpduUtils.encodeMsisdnAsAddressField(address, isSmscNumber);
		// Make sure that the top bit of Type-of-Address is set!
		assertEquals("Type-of-Address must have its top bit set.", 1 << 7, encodedMsisdn[1] & (1 << 7));
		PduInputStream in = new PduInputStream(encodedMsisdn);
		String decodedMsisdn = TpduUtils.decodeMsisdnFromAddressField(in, isSmscNumber);
		assertEquals("Semi-octet codec (" + (isSmscNumber ? "SMSC" : "non-SMSC") + ") failed.  Encoded = [" + HexUtils.encode(encodedMsisdn) + "]", address, decodedMsisdn);
	}
}
