package fast.common.replay;

import fast.common.core.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import quickfix.DataDictionary;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class TestMiniFixHelper {

    private Map<String,Object> agentParams =new HashMap<>();
    private MiniFixHelper miniFixHelper;

    @Before
    public void setUp() throws Exception {
        agentParams.put("TagsWithIgnoreValueCheck",new ArrayList<>(Arrays.asList(628,8020,8037,-20203,-20204,58)));
        agentParams.put("TagsWithClOrdIDGeneratorAndCheck",new ArrayList<>(Arrays.asList(11,10081,41,-1,-2)));
        agentParams.put("TagsWithOrderIDGeneratorAndCheck",new ArrayList<>(Arrays.asList(37,17,11210,-11210)));
        agentParams.put("TagsWithDateTimeGeneratorAndCheck",new ArrayList<>(Arrays.asList(52,60,10014,10080,8012,75,122,8023,64)));
        agentParams.put("TagsWithTimeInForceGeneratorAndCheck",new ArrayList<>(Arrays.asList(126)));
        agentParams.put("TagsWithRangeCheck",new HashMap<String,String>(){{
            put("-20204","1.0");
            put("-20203","1.0");
            put("-44","1.0");
            put("6","0.1");
            put("31","0.1");
            put("44","0.1");
            put("426","0.1");
        }});
        agentParams.put("TagsWithProfilesCheck",new ArrayList<>(Arrays.asList(8019)));
        agentParams.put("TimeRangeCheck","900");
        agentParams.put("TimeSmallRangeCheck","1");
        agentParams.put("SkipTimestampFormatCheck","1");
        agentParams.put("C4TimeZone","Europe/London");
        miniFixHelper = new MiniFixHelper(agentParams);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void checkReceive() throws Exception {
        ReplayManager replayManager = new ReplayManager(agentParams,Configurator.getInstance().getConfigFolder(),null, "");
        replayManager.startDateTime = new Date();
        replayManager.actualStartDateTime = new Date();
        ReplayScenario replayScenario = new ReplayScenario(replayManager,"18172BDo7bs",null);
        DataDictionary fixDictionary = new DataDictionary(Paths.get("config/").resolve("quickfix_spec/FIX44.xml").toString());

        String send_data = "8=FIX.4.4\u00019=1043\u000135=8\u000149=CITI\u000156=ACCIVAL\u0001627=1\u0001628=C4X_NY5_I1\u0001-151515=\u0001-10892=NAM\u0001-7575=20180621\u0001-426=0.0\u0001-425=0.0\u0001-101=USSOR\u0001-49=CL.GLOBAL\u0001-33=;RC:E;\u0001-12=1529591839332\u0001-6=1529591839331\u0001-1=2:172ACCIVAL\u00011=ACIVLAE\u00016=0.0\u000111=2:172ACCIVAL\u000114=0.0\u000117=18172A82ezd\u000118=5\u000121=1\u000131=-1.0\u000132=0.0\u000137=18172BDo7bs\u000138=50.0\u000139=0\u000140=2\u000144=119.25\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19.336000\u000163=0\u0001132=-1.0\u0001133=-1.0\u0001150=0\u0001151=50.0\u0001421=USA\u0001455=20853531\u0001528=A\u0001779=20180621-14:37:19.330\u00011727=729\u00018001=ACCIVAL\u00018003=USSOR\u00018004=ACIVLAE\u00018007=?\u00018008=QDMA\u00018012=20180621\u00018015=ACCIVAL\u00018019=RestrictedAccountEnabled=Y;AutoMergeF=R;OrderPlacer=1007396771;PrcDeviation=0.2;AutoTktF=N;IntraDayEviction=Y;ContinuousOrder=Y;OmsBbeEnabled=Y;\u00018023=20180621-21:00:00\u00018037=13\u000110013=;CC:2:172ACCIVAL;DT:00143719;\u000110031=0A\u000110044=;8018=QFFC_US1_GW1^^^~\u000110077=1\u000110079=2\u000110083=QFF\u000110092=7\u000110184=18172BDo7bs\u000110201=USFF\u000110202=USDMA\u000110203=USQFF\u000110204=USSOR\u000110515=6\u000110518=6\u000110673=false\u000110721=-1.0\u000110722=-1.0\u000110895=1007396771\u000111022=20180621-14:37:19.326\u000111027=N\u000111032=0\u000111042=3\u000111210=18172BDo7bs\u000112034=Y\u000199999=true\u000110=140\u0001";
        quickfix.Message send_msg = new quickfix.Message(send_data, fixDictionary, false);
        miniFixHelper.processStepData(replayScenario,send_msg,true);

        String orig_data = "8=FIX.4.2\u00019=0219\u000135=8\u000149=CITI\u000156=ACCIVAL\u000134=441\u000152=20180621-14:37:19\u0001129=JM73652\u00016=0\u000111=2\u000114=0\u000115=USD\u000117=18172A82ezd\u000120=0\u000131=0\u000132=0\u000137=18172BDo7bs\u000138=50\u000139=0\u000140=2\u000144=119.2500\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19\u000163=0\u0001150=0\u0001151=50\u000110=082\u0001";
        quickfix.Message org_msg = new quickfix.Message(orig_data, fixDictionary, false);
        StructuredTagList ms = miniFixHelper.processStepData(replayScenario,org_msg,false);;

        String expected_data = "8=FIX.4.2\u00019=301\u000135=8\u000134=441\u000149=CITI\u000152=20180621-14:37:19\u000156=ACCIVAL\u0001129=JM73652\u00016=0\u000111=C1/18172BDo7bs/replay20190117162912\u000114=0\u000115=USD\u000117=S3/18172BDo7bs/replay20190117162912\u000120=0\u000131=0\u000132=0\u000138=50\u000139=0\u000140=2\u000144=119.2500\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19\u000163=0\u0001150=0\u0001151=50\u000110=157\u0001";
        quickfix.Message expected_msg = new quickfix.Message(expected_data, fixDictionary, false);

        String received_data = "8=FIX.4.2\u00019=370\u000135=8\u000134=2\u000149=CITI\u000152=20181126-06:31:09.999\u000156=ACCIVAL\u0001-10=1543213869997000\u00016=0.0\u000111=C1/18172BDo7bs/replay20190117162912\u000114=0\u000117=S3/18172BDo7bs/replay20190117162912\u000120=0\u000131=-1.0\u000132=0\u000138=50\u000139=0\u000140=2\u000144=119.25\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20181126-06:31:09.532\u000163=0\u0001150=0\u0001151=50\u0001455=20853531\u00018003=USSOR\u00018023=20181126-06:31:09.000\u00018087=2\u000110=240\u0001";
        quickfix.Message received_msg = new quickfix.Message(received_data, fixDictionary, false);
        LockedBufferOfReceivedMessages receivedMessages = new LockedBufferOfReceivedMessages();
        receivedMessages.buffer.add(received_msg);
        try{
            quickfix.Message msg = miniFixHelper.checkReceive(replayScenario,expected_msg,receivedMessages,ms,3,fixDictionary);
        }catch (Exception e){
            assertTrue(e.getMessage().contains("8=FIX.4.2\u00019=331\u000135=8\u000134=2\u000149=CITI\u000152=20181126-06:31:09.999\u000156=ACCIVAL\u0001-10=1543213869997000\u00016=0.0\u000111=C1/18172BDo7bs/replay20190117162912\u000114=0\u000117=S3/18172BDo7bs/replay20190117162912\u000120=0\u000131=-1.0\u000132=0\u000138=50\u000139=0\u000140=2\u000144=119.25\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20181126-06:31:09.532\u000163=0\u0001150=0\u0001151=50\u0001455=20853531\u00018003=USSOR\u00018023=20181126-06:31:09.000\u00018087=2\u000110=016\u0001"));
        }
    }

    @Test
    public void processStepData() throws Exception {
        ReplayManager replayManager = new ReplayManager(agentParams,Configurator.getInstance().getConfigFolder(),null, "");
        replayManager.startDateTime = new Date();
        replayManager.actualStartDateTime = new Date();
        ReplayScenario replayScenario = new ReplayScenario(replayManager,"18171BDo77t",null);
        DataDictionary fixDictionary = new DataDictionary(Paths.get("config/").resolve("quickfix_spec/FIX44.xml").toString());
        String send_data = "8=FIX.4.4\u00019=1175\u000135=8\u000149=CITICN\u000156=MILENCN1\u000157=MLB\u0001627=1\u0001628=QFFX_US86_I2\u0001-151515=7400=20180620-19:45:00;7401=20180620-20:00:00.000;7411=0;XL:1529524800000000000;\u0001-10892=NAM\u0001-7575=20180620\u0001-426=0.0\u0001-425=0.0\u0001-101=CDDSA\u0001-50=MLB\u0001-49=CL.GLOBAL\u0001-33=;RC:E;\u0001-12=1529523097197\u0001-6=1529523097195\u0001-1=4a2MRA25298hync1c:171MILENCN1\u00011=MLPCNAE\u00016=0.0\u000111=4a2MRA25298hync1c:171MILENCN1\u000114=0.0\u000117=18171BVacg1\u000118=1\u000121=1\u000131=-1.0\u000132=0.0\u000137=18171BDo77t\u000138=295.0\u000139=0\u000140=1\u000144=-1.0\u000154=1\u000155=FM.TO\u000159=0\u000160=20180620-19:31:37.000000\u000163=0\u0001126=20180620-20:00:00\u0001132=-1.0\u0001133=-1.0\u0001150=0\u0001151=295.0\u0001421=CAN\u0001455=32263218\u0001528=A\u0001779=20180620-19:31:37.193\u00011727=729\u00018001=MILENCN1\u00018003=CDDSA\u00018004=MLPCNAE\u00018007=ITGSMRT|\u00018008=QDMA\u00018012=20180620\u00018015=MILENCN1\u00018019=RestrictedAccountEnabled=Y;AutoMergeF=R;OrderPlacer=1003976315;StampID=CIT29IT;PrcDeviation=.75;ContinuousOrder=Y;OmsBbeEnabled=Y;\u00018023=20180620-21:00:00\u00018037=5\u000110013=;CC:4a2MRA25298hync1c:171MILENCN1;DT:00193137;\u000110031=01\u000110044=;8018=QFFC_US1_GW1^^^~\u000110077=1\u000110079=2\u000110083=QFF\u000110092=7\u000110201=USFF\u000110202=CDDSA\u000110203=USQFF\u000110204=ITGC\u000110515=6\u000110673=false\u000110721=-1.0\u000110722=-1.0\u000110895=1003976315\u000111022=20180620-19:31:37.149\u000111027=N\u000111032=0\u000111042=-1\u000111210=18171BDo77t\u000112034=N\u000110=188\u0001";
        quickfix.Message send_msg = new quickfix.Message(send_data, fixDictionary, false);
        miniFixHelper.processStepData(replayScenario,send_msg,true);
        String send_expected = send_msg.toString();
        assertTrue(send_expected.contains("replay"));

        String receive_data = "8=FIX.4.2\u00019=0240\u000135=8\u000149=CITICN\u000156=MILENCN1\u000134=1025\u000152=20180620-19:31:37\u000157=MLB\u00016=0\u000111=4a2MRA25298hync1c\u000114=0\u000115=CAD\u000117=18171BVacg1\u000120=0\u000122=5\u000131=0\u000132=0\u000137=18171BDo77t\u000138=295\u000139=0\u000140=1\u000147=A\u000148=FM.TO\u000154=1\u000155=FM.TO\u000159=0\u000160=20180620-19:31:37\u000163=0\u0001150=0\u0001151=295\u000110=115\u0001";
        quickfix.Message receive_msg = new quickfix.Message(receive_data, fixDictionary, false);
        miniFixHelper.processStepData(replayScenario,receive_msg,false);
        String receive_expected = receive_msg.toString();
        assertTrue(receive_expected.contains("replay"));
    }

    @Test
    public void extractScenarioId() throws Exception {
        ReplayManager replayManager = new ReplayManager(agentParams,Configurator.getInstance().getConfigFolder(),null, "");
        replayManager.startDateTime = new Date();
        replayManager.actualStartDateTime = new Date();
        ReplayScenario replayScenario = new ReplayScenario(replayManager,"18172BDo7bs",null);
        DataDictionary fixDictionary = new DataDictionary(Paths.get("config/").resolve("quickfix_spec/FIX44.xml").toString());

        String data = "8=FIX.4.4\u00019=1043\u000135=8\u000149=CITI\u000156=ACCIVAL\u0001627=1\u0001628=C4X_NY5_I1\u0001-151515=\u0001-10892=NAM\u0001-7575=20180621\u0001-426=0.0\u0001-425=0.0\u0001-101=USSOR\u0001-49=CL.GLOBAL\u0001-33=;RC:E;\u0001-12=1529591839332\u0001-6=1529591839331\u0001-1=2:172ACCIVAL\u00011=ACIVLAE\u00016=0.0\u000111=2:172ACCIVAL\u000114=0.0\u000117=18172A82ezd\u000118=5\u000121=1\u000131=-1.0\u000132=0.0\u000137=18172BDo7bs\u000138=50.0\u000139=0\u000140=2\u000144=119.25\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19.336000\u000163=0\u0001132=-1.0\u0001133=-1.0\u0001150=0\u0001151=50.0\u0001421=USA\u0001455=20853531\u0001528=A\u0001779=20180621-14:37:19.330\u00011727=729\u00018001=ACCIVAL\u00018003=USSOR\u00018004=ACIVLAE\u00018007=?\u00018008=QDMA\u00018012=20180621\u00018015=ACCIVAL\u00018019=RestrictedAccountEnabled=Y;AutoMergeF=R;OrderPlacer=1007396771;PrcDeviation=0.2;AutoTktF=N;IntraDayEviction=Y;ContinuousOrder=Y;OmsBbeEnabled=Y;\u00018023=20180621-21:00:00\u00018037=13\u000110013=;CC:2:172ACCIVAL;DT:00143719;\u000110031=0A\u000110044=;8018=QFFC_US1_GW1^^^~\u000110077=1\u000110079=2\u000110083=QFF\u000110092=7\u000110184=18172BDo7bs\u000110201=USFF\u000110202=USDMA\u000110203=USQFF\u000110204=USSOR\u000110515=6\u000110518=6\u000110673=false\u000110721=-1.0\u000110722=-1.0\u000110895=1007396771\u000111022=20180621-14:37:19.326\u000111027=N\u000111032=0\u000111042=3\u000111210=18172BDo7bs\u000112034=Y\u000199999=true\u000110=140\u0001";
        quickfix.Message msg = new quickfix.Message(data, fixDictionary, false);
        String id = miniFixHelper.extractScenarioId(msg);
        assertEquals(null, id);
    }

    @Test
    public void cleanUpScenarioByFindingItsMessages() throws Exception {
        ReplayManager replayManager = new ReplayManager(agentParams,Configurator.getInstance().getConfigFolder(),null, "");
        replayManager.startDateTime = new Date();
        replayManager.actualStartDateTime = new Date();
        ReplayScenario replayScenario = new ReplayScenario(replayManager,"18172BDo7bs",null);
        DataDictionary fixDictionary = new DataDictionary(Paths.get("config/").resolve("quickfix_spec/FIX44.xml").toString());

        String send_data = "8=FIX.4.4\u00019=1043\u000135=8\u000149=CITI\u000156=ACCIVAL\u0001627=1\u0001628=C4X_NY5_I1\u0001-151515=\u0001-10892=NAM\u0001-7575=20180621\u0001-426=0.0\u0001-425=0.0\u0001-101=USSOR\u0001-49=CL.GLOBAL\u0001-33=;RC:E;\u0001-12=1529591839332\u0001-6=1529591839331\u0001-1=2:172ACCIVAL\u00011=ACIVLAE\u00016=0.0\u000111=2:172ACCIVAL\u000114=0.0\u000117=18172A82ezd\u000118=5\u000121=1\u000131=-1.0\u000132=0.0\u000137=18172BDo7bs\u000138=50.0\u000139=0\u000140=2\u000144=119.25\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19.336000\u000163=0\u0001132=-1.0\u0001133=-1.0\u0001150=0\u0001151=50.0\u0001421=USA\u0001455=20853531\u0001528=A\u0001779=20180621-14:37:19.330\u00011727=729\u00018001=ACCIVAL\u00018003=USSOR\u00018004=ACIVLAE\u00018007=?\u00018008=QDMA\u00018012=20180621\u00018015=ACCIVAL\u00018019=RestrictedAccountEnabled=Y;AutoMergeF=R;OrderPlacer=1007396771;PrcDeviation=0.2;AutoTktF=N;IntraDayEviction=Y;ContinuousOrder=Y;OmsBbeEnabled=Y;\u00018023=20180621-21:00:00\u00018037=13\u000110013=;CC:2:172ACCIVAL;DT:00143719;\u000110031=0A\u000110044=;8018=QFFC_US1_GW1^^^~\u000110077=1\u000110079=2\u000110083=QFF\u000110092=7\u000110184=18172BDo7bs\u000110201=USFF\u000110202=USDMA\u000110203=USQFF\u000110204=USSOR\u000110515=6\u000110518=6\u000110673=false\u000110721=-1.0\u000110722=-1.0\u000110895=1007396771\u000111022=20180621-14:37:19.326\u000111027=N\u000111032=0\u000111042=3\u000111210=18172BDo7bs\u000112034=Y\u000199999=true\u000110=140\u0001";
        quickfix.Message send_msg = new quickfix.Message(send_data, fixDictionary, false);
        miniFixHelper.processStepData(replayScenario,send_msg,true);

        String orig_data = "8=FIX.4.2\u00019=0219\u000135=8\u000149=CITI\u000156=ACCIVAL\u000134=441\u000152=20180621-14:37:19\u0001129=JM73652\u00016=0\u000111=2\u000114=0\u000115=USD\u000117=18172A82ezd\u000120=0\u000131=0\u000132=0\u000137=18172BDo7bs\u000138=50\u000139=0\u000140=2\u000144=119.2500\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19\u000163=0\u0001150=0\u0001151=50\u000110=082\u0001";
        quickfix.Message org_msg = new quickfix.Message(orig_data, fixDictionary, false);
        StructuredTagList ms = miniFixHelper.processStepData(replayScenario,org_msg,false);;

        String expected_data = "8=FIX.4.2\u00019=301\u000135=8\u000134=441\u000149=CITI\u000152=20180621-14:37:19\u000156=ACCIVAL\u0001129=JM73652\u00016=0\u000111=C1/18172BDo7bs/replay20190117162912\u000114=0\u000115=USD\u000117=S3/18172BDo7bs/replay20190117162912\u000120=0\u000131=0\u000132=0\u000138=50\u000139=0\u000140=2\u000144=119.2500\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20180621-14:37:19\u000163=0\u0001150=0\u0001151=50\u000110=157\u0001";
        quickfix.Message expected_msg = new quickfix.Message(expected_data, fixDictionary, false);

        String received_data = "8=FIX.4.2\u00019=370\u000135=8\u000134=2\u000149=CITI\u000152=20181126-06:31:09.999\u000156=ACCIVAL\u0001-10=1543213869997000\u00016=0.0\u000111=C1/18172BDo7bs/replay20190117162912\u000114=0\u000117=S3/18172BDo7bs/replay20190117162912\u000120=0\u000131=-1.0\u000132=0\u000138=50\u000139=0\u000140=2\u000144=119.25\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20181126-06:31:09.532\u000163=0\u0001150=0\u0001151=50\u0001455=20853531\u00018003=USSOR\u00018023=20181126-06:31:09.000\u00018087=2\u000110=240\u0001";
        quickfix.Message received_msg = new quickfix.Message(received_data, fixDictionary, false);
        LockedBufferOfReceivedMessages receivedMessages = new LockedBufferOfReceivedMessages();
        receivedMessages.buffer.add(received_msg);

        ArrayList<quickfix.Message> foundMessages = miniFixHelper.cleanUpScenarioByFindingItsMessages(expected_msg, receivedMessages, ms);
        assertEquals("[8=FIX.4.2\u00019=331\u000135=8\u000134=2\u000149=CITI\u000152=20181126-06:31:09.999\u000156=ACCIVAL\u0001-10=1543213869997000\u00016=0.0\u000111=C1/18172BDo7bs/replay20190117162912\u000114=0\u000117=S3/18172BDo7bs/replay20190117162912\u000120=0\u000131=-1.0\u000132=0\u000138=50\u000139=0\u000140=2\u000144=119.25\u000147=A\u000154=2\u000155=IYF\u000159=0\u000160=20181126-06:31:09.532\u000163=0\u0001150=0\u0001151=50\u0001455=20853531\u00018003=USSOR\u00018023=20181126-06:31:09.000\u00018087=2\u000110=016\u0001]", foundMessages.toString());
    }
}
