package fr.tduf.cli.tools;

import org.junit.Test;

public class DatabaseToolTest {

    @Test(expected = NullPointerException.class)
    public void checkCarPhysicsDataLine_whenNullLine_shouldThrowException(){
        // GIVEN-WHEN
        DatabaseTool.checkCarPhysicsDataLine(null);

        // THEN:NPE
    }

    @Test(expected = RuntimeException.class)
    public void checkCarPhysicsDataLine_whenIncorrectLine_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseTool.checkCarPhysicsDataLine("125654;1234456;1254");

        // THEN:RE
    }

    @Test
    public void checkCarPhysicsDataLine_whenCorrectLine_shouldNotThrowException() {
        // GIVEN-WHEN
        DatabaseTool.checkCarPhysicsDataLine("1139121456;77060;60733407;59733427;78900264;1;43055;59368917;60733407;1292203257;60733407;53338427;57356917;57356917;543629;553709;6210;411;5500;588;4500;6500;54356917;563531;6;4,10;4,17;2,34;1,52;1,14;0,86;0,69;0;2;5355936;350;347;5355936;61338424;0;1996;5603;1993;1869;1869;3385;965;1253;2767;500;500;380;480;130;160;95;95;95;95;105;30;52;0;0,380;2,5;45;50;60;1,4;1,4;1,7;2900;1;74739427;92261406;59533427;5566317;7,2;0;161;0;0;0;0;0;0;0;980;98;60;113;82,1;50;100;75;-1;1;231;237;1;100;237;79;");

        // THEN:no exception
    }
}