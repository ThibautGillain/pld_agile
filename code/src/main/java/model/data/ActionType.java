package model.data;


/**
 * This Enum serves to differentiate between picking a package up before the
 * delivery and delivering the package.
 */
public enum ActionType {

    /**
     * If the cyclist pick the parcel up to start a delivery this action type
     * should be used.
     */
    PICK_UP,

    /**
     * When delivering this type should be used.
     */
    DELIVERY,
    /**
     * Start point of the cyclist
     */
    BASE;

    /**
     * Returns the opposing actionType of an actionType.
     *
     * @param actionType the actionTtype
     * @return the opposite
     */
//    public ActionType other(ActionType actionType) {
//        if (actionType == ActionType.DELIVERY) {
//            return PICK_UP;
//        } else {
//            return DELIVERY;
//        }
//    }
}