package model.data;

/**
 * This class serves as a container for all data a project contains.
 * It offers getter and setter methods for all stored data classes.
 */
public interface ProjectData {

    Tour getTour();

    void setTour(Tour newTour);

    void setGraph( final Graph graph);

    void getGraph();

    

}
