package model.data;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;


import java.util.ArrayList;
import java.util.List;

/**
 * Test for Point
 */
public class PointTest {

    private final int ID_NEG = -1;
    private final int ID_TEST = 20;
    private final double LAT_SMALL = -91;
    private final double LAT_BIG = 91;
    private final double LAT_TEST = 45;
    private final double LNG_SMALL = -181;
    private final double LNG_BIG = 181;
    private final double LNG_TEST = 45;
    private List<Segment> TEST_SEGMENTS = new ArrayList<Segment>();
    private final int TEST_ID = 25175791;
    private final double TEST_LATITUDE = 2.2;
    private final double TEST_LONGITUDE = 48.1;
    private Point subject = null;

    @Before
    public void setUp() {
        subject = new Point(TEST_ID, TEST_LATITUDE, TEST_LONGITUDE);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetLengthTo_correctInput_validCalculation() {

        // Arrange
        final double CORRECT_LENGTH = 69.979805;
        subject.addNeighbour(new Segment(25175791, 25175778, 69.979805, "Rue Danton"));
        subject.addNeighbour(new Segment(25175791, 2117622723, 136.00636, "Rue de l'Abondance\""));

        // Act
        final double test_length = subject.getLengthTo(25175778);

        // Assert
        assertEquals("wrong length", CORRECT_LENGTH, test_length, 0);

    }

    @Test
    public void testGetLengthTo_pointNotReachable_IllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("point not reachable via one segment");
        final long wrong_id = 2365376415L;

        // Act
        final double test_length = subject.getLengthTo(wrong_id);

        // Assert -> via annotation

    }


    @Test
    public void testCTOR_idNegative_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id is negative");

        // Act
        new Point(ID_NEG, LAT_TEST, LNG_TEST);//,SEGMENTS_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_latitudeTooSmall_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("latitude is too small");

        // Act
        new Point(ID_TEST, LAT_SMALL, LNG_TEST);//,SEGMENTS_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_latitudeTooGreat_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("latitude is too great");

        // Act
        new Point(ID_TEST, LAT_BIG, LNG_TEST);//,SEGMENTS_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_longitudeTooSmall_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("longitude is too small");

        // Act
        new Point(ID_TEST, LAT_TEST, LNG_SMALL);//,SEGMENTS_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_longitudeTooGreat_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("longitude is too great");

        // Act
        new Point(ID_TEST, LAT_TEST, LNG_BIG);//,SEGMENTS_TEST);

        // Assert via annotation
    }

    @Test
    public void testGetId_validCall_correctId() {

        // Arrange

        // Act
        final long id = subject.getId();

        // Assert
        assertEquals("Wrong id", id, TEST_ID);
    }

    @Test
    public void testGetLatitude_validCall_correctId() {

        // Arrange

        // Act
        final double latitude = subject.getLatitude();

        // Assert
        assertEquals("Wrong id", latitude, TEST_LATITUDE, 0);
    }

    @Test
    public void testGetLongitude_validCall_correctId() {

        // Arrange

        // Act
        final double longitude = subject.getLongitude();

        // Assert
        assertEquals("Wrong id", longitude, TEST_LONGITUDE, 0);
    }

    @Test
    public void testGetNeighbourSegments_validCall_correctId() {

        // Arrange
        final Segment segment1 = new Segment(25175791, 25175778, 69.979805, "Rue Danton");
        final Segment segment2 = new Segment(25175791, 2117622723, 136.00636, "Rue de l'Abondance\"");
        TEST_SEGMENTS.add(segment1);
        TEST_SEGMENTS.add(segment2);
        subject.addNeighbour(segment1);
        subject.addNeighbour(segment2);

        // Act
        final List neighbourSegments = subject.getSegments();

        // Assert
        System.out.println(TEST_SEGMENTS);
        assertEquals("Wrong segments", neighbourSegments, TEST_SEGMENTS);
    }


}