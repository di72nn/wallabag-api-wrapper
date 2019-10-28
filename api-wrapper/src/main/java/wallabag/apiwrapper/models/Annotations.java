package wallabag.apiwrapper.models;

import wallabag.apiwrapper.WallabagService;

import java.util.List;

/**
 * The {@code Annotations} class represents the response to
 * {@link WallabagService#getAnnotations(int)} call.
 * <p>This is a direct response mapping, hence the member names.
 */
public class Annotations {

    /** The total number of annotations. */
    public int total;

    /** The annotations. */
    public List<Annotation> rows;

    @Override
    public String toString() {
        return "Annotations{" +
                "total=" + total +
                ", rows" + (rows == null ? "=null" : ("[" + rows.size() + "]")) +
                '}';
    }

}
