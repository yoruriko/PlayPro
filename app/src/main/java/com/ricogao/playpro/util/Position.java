package com.ricogao.playpro.util;

/**
 * Created by ricogao on 2017/4/6.
 */

public enum Position {

    CENTRE_FORWARD(0, "Centre Forward"),
    CENTRE_MIDFIELD(1, "Centre Midfield"),
    WING_MIDFIELD(2, "Wing Midfield"),
    CENTRE_BACK(3, "Centre Back"),
    WING_BACK(4, "Wing Back");

    private int positionCode;
    private String positionName;
    public static String[] positions = {CENTRE_FORWARD.getPositionName(), CENTRE_MIDFIELD.getPositionName(), WING_MIDFIELD.getPositionName()
            , CENTRE_BACK.getPositionName(), WING_BACK.getPositionName()};

    Position(int code, String name) {
        positionCode = code;
        this.positionName = name;
    }

    public int getPositionCode() {
        return positionCode;
    }

    public static Position getPositionFromCode(int code) {
        switch (code) {
            case 0:
                return CENTRE_FORWARD;
            case 1:
                return CENTRE_MIDFIELD;
            case 2:
                return WING_MIDFIELD;
            case 3:
                return CENTRE_BACK;
            case 4:
                return WING_BACK;
            default:
                return CENTRE_FORWARD;
        }
    }

    public String getPositionName() {
        return positionName;
    }
}
