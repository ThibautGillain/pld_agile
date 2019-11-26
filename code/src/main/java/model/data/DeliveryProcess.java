package model.data;

/**
 * This class represents one Deliveryprocess. For this it has two ActionPoints one Pick Up point and one Delivery Point
 */
public class DeliveryProcess {

    /**
     * pickUp point
     */
    ActionPoint pickUP;

    /**
     * Delivery point
     */
    ActionPoint delivery;

    /**
     * Instatiates a Delivery process
     * @param pickUP pickUp point of the delivery
     * @param delivery delivery point of the deliver
     */
    public DeliveryProcess(ActionPoint pickUP, ActionPoint delivery){
        this.pickUP=pickUP;
        this.delivery=delivery;
    }

    public ActionPoint getPickUP() {
        return pickUP;
    }

    public ActionPoint getDelivery() {
        return delivery;
    }
}
