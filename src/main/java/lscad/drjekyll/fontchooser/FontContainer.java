package lscad.drjekyll.fontchooser;

import java.awt.Font;


/**
 * Created by dheid on 4/1/17.
 */
public interface FontContainer {

    String getSelectedStyle();

    float getSelectedSize();

    String getSelectedFamily();
    
    int getStyleOptions();

    Font getSelectedFont();

    void setSelectedFont(Font font);

    void setPreviewFont(Font font);

}
