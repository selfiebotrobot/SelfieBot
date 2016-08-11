package com.endurancerobots.selfiebotdroid.Common;

public class Commands {
    //public final static byte UP=115,LEFT=97,DOWN=119,RIGHT=100,CLOSE=113,NOP=001;
    public final static byte UP='S',LEFT='A',DOWN='W',RIGHT='D',STOP='X',CLOSE=113,NOP=001;
   // com=W - вверх, com=S - вниз, com=A - влево, com=D - вправо, com=X
    public static final byte NoOpMsg[] = {NOP,NOP,NOP,NOP,NOP};
    /*
        public static final byte UpMsg[] = {UP,UP,UP,UP,UP};
        public static final byte DownMsg[] = {DOWN,DOWN,DOWN,DOWN,DOWN};
        public static final byte LeftMsg[] = {LEFT,LEFT,LEFT,LEFT,LEFT};
        public static final byte RightMsg[] = {RIGHT,RIGHT,RIGHT,RIGHT,RIGHT};
    */
    public static final byte UpMsg[] = {'c','o','m','=',UP};
    public static final byte DownMsg[] = {'c','o','m','=',DOWN};
    public static final byte LeftMsg[] = {'c','o','m','=',LEFT};
    public static final byte RightMsg[] = {'c','o','m','=',RIGHT};
    public static final byte StopMsg[] = {'c','o','m','=',STOP};

    public static final int CMD_LENGTH = 5;
    private static int CTRL_BYTE=4;
    public static boolean isCommand(byte[] cmd) {
        if(cmd.length!=CMD_LENGTH) return false;
        byte b=cmd[CTRL_BYTE];
        return (b==NOP || b==UP || b==DOWN || b==LEFT || b==RIGHT || b== STOP);
    }
    public static byte controlByte(byte[] cmd)
    {
        return cmd[CTRL_BYTE];
    }
    public static String decode(byte[] cmd) {
        switch (cmd[CTRL_BYTE]){
            case UP:
                return ("Command: UP (" + cmd[CTRL_BYTE] + ")");
            case LEFT:
                return("Command: LEFT (" + cmd[CTRL_BYTE] + ")");
            case DOWN:
                return("Command: DOWN (" + cmd[CTRL_BYTE] + ")");
            case RIGHT:
                return("Command: RIGHT (" + cmd[CTRL_BYTE] + ")");
             case STOP:
                return("Command: STOP (" + cmd[CTRL_BYTE] + ")");
            case CLOSE:
                return("Command: CLOSE CONNECTION (" + cmd[CTRL_BYTE] + ")");
            default:
                return("Unknown command: (" + cmd[CTRL_BYTE] + ")");
        }
    }
}
