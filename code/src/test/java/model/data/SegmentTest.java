package model.data;

import model.genData.Segment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test for Segment
 */
public class SegmentTest {

    private final float NEGATIVE_LENGTH = -1;
    private final float ZERO_LENGTH = 0;
    private final String NAME_TEST = "rue mermoz";
    private final int LENGTH_TEST = 4;
    private final int ID_ORIGIN_TEST = 25175778;
    private final int ID_END_TEST = 25175791;
    private final int NEGATIVE_ID_ORIGIN = -1;
    private final int NEGATIVE_ID_END = -1;
    private final int ID_NOT_EXIST = -1;
    private Segment SEGMENT_TEST = new Segment(ID_ORIGIN_TEST, ID_END_TEST, LENGTH_TEST, NAME_TEST);


    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testCTOR_lengthNegative_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("length is negative");

        // Act
        new Segment(ID_ORIGIN_TEST, ID_END_TEST, NEGATIVE_LENGTH, NAME_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_lengthZero_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("length is zero");

        // Act
        new Segment(ID_ORIGIN_TEST, ID_END_TEST, ZERO_LENGTH, NAME_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_NameNull_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name is null");

        // Act
        new Segment(ID_ORIGIN_TEST, ID_END_TEST, LENGTH_TEST, null);


        // Assert via annotation
    }

    /**
     * @Test public void testCTOR_NameEmpty_throwsIllegalArgumentException() {
     * <p>
     * // Arrange
     * thrown.expect(IllegalArgumentException.class);
     * thrown.expectMessage("name is empty");
     * <p>
     * // Act
     * new Segment(ID_ORIGIN_TEST,ID_END_TEST,LENGTH_TEST,EMPTY_STRING);
     * <p>
     * // Assert via annotation
     * }
     */

    @Test
    public void testCTOR_Id_originNegative_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id_origin is negative");

        // Act
        new Segment(NEGATIVE_ID_ORIGIN, ID_END_TEST, LENGTH_TEST, NAME_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_Id_endNegative_throwsIllegalArgumentException() {

        // Arrange
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id_end is negative");

        // Act
        new Segment(ID_ORIGIN_TEST, NEGATIVE_ID_END, LENGTH_TEST, NAME_TEST);

        // Assert via annotation
    }

    @Test
    public void testCTOR_GetterId_origin() {

        // Arrange

        // Act
        SEGMENT_TEST.getIdOrigin();

        // Assert via annotation
    }

    @Test
    public void testCTOR_GetterId_end() {

        // Arrange

        // Act
        SEGMENT_TEST.getIdEnd();

        // Assert via annotation
    }

    @Test
    public void testCTOR_GetterLength() {

        // Arrange

        // Act
        SEGMENT_TEST.getLength();

        // Assert via annotation
    }

    @Test
    public void testCTOR_GetterName() {

        // Arrange

        // Act
        SEGMENT_TEST.getName();

        // Assert via annotation
    }

}
