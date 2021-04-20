import java.io.Serializable;

public class Cell implements Serializable {
    String row_column;
    String content;
    String style_flags;

    public Cell(String row_column, String content, String style_flags) {
        this.row_column = row_column;
        this.content = content;
        this.style_flags = style_flags;
    }
}
