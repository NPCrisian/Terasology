/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.widgets;

import com.google.common.collect.Lists;
import org.terasology.utilities.Assets;
import org.terasology.audio.StaticSound;
import org.terasology.input.MouseInput;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

import java.util.List;

/**
 * A widget displaying a clickable button, containing text and an optional image
 */
public class UIButton extends CoreWidget {
    public static final String DOWN_MODE = "down";

    /**
     * The {@link Binding} containing the {@link TextureRegion} corresponding to the image shown on the button
     */
    @LayoutConfig
    private Binding<TextureRegion> image = new DefaultBinding<>();

    /**
     * The {@code Binding} containing the text to be shown on the button
     */
    @LayoutConfig
    private Binding<String> text = new DefaultBinding<>("");

    /**
     * The {@code Binding} containing the {@link StaticSound} to be played when the button is clicked
     */
    @LayoutConfig
    private Binding<StaticSound> clickSound = new DefaultBinding<>(Assets.getSound("engine:click").get());

    /**
     * The {@code Binding} containing the float representing the volume of the click sound, 1.0 by default
     */
    @LayoutConfig
    private Binding<Float> clickVolume = new DefaultBinding<>(1.0f);

    /**
     * Whether the button is currently being pressed
     */
    private boolean down;

    /**
     * A {@link List} of listeners subscribed to this button
     */
    private List<ActivateEventListener> listeners = Lists.newArrayList();

    /**
     * An {@link InteractionListener} that listens for clicks on the button
     */
    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                down = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                if (isMouseOver()) {
                    if (getClickSound() != null) {
                        getClickSound().play(getClickVolume());
                    }
                    activate();
                }
                down = false;
            }
        }
    };

    /**
     * The default constructor
     */
    public UIButton() {
    }

    /**
     * The parameterized constructor
     *
     * @param id The id assigned to this {@code UIButton}
     */
    public UIButton(String id) {
        super(id);
    }

    /**
     * The parameterized constructor
     *
     * @param id The id assigned to this {@code UIButton}
     * @param text The text shown on the {@code UIButton}
     */
    public UIButton(String id, String text) {
        super(id);
        this.text.set(text);
    }

    /**
     * The parameterized constructor
     *
     * @param id The id assigned to this {@code UIButton}
     * @param text The {@code Binding} containing the text shown on the {@code UIButton}
     */
    public UIButton(String id, Binding<String> text) {
        super(id);
        this.text = text;
    }

    /**
     * Handles how the {@code UIButton} is drawn - called every frame
     *
     * @param canvas The {@link Canvas} on which the {@code UIButton} is drawn
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (image.get() != null) {
            canvas.drawTexture(image.get());
        }
        canvas.drawText(text.get());
        if (isEnabled()) {
            canvas.addInteractionRegion(interactionListener);
        }
    }

    /**
     * Retrieves the preferred content size of the {@code UIButton}
     *
     * @param canvas The {@code Canvas} on which the {@code UIButton} is drawn
     * @param areaHint A hint as to how the {@code UIButton} should be laid out
     * @return A {@link Vector2i} representing the preferred content size of the {@code UIButton}
     */
    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, text.get(), areaHint.getX());
        return font.getSize(lines);
    }

    /**
     * Retrieves the current mode of the {@code UIButton}
     * <p><ul>
     * <li> DISABLED_MODE - The [@code UIButton} is disabled
     * <li> DOWN_MODE - The {@code UIButton} is being pressed
     * <li> HOVER_MODE - The mouse is hovering over the {@code UIButton}
     * <li> DEFAULT_MODE - Default mode if none of the others are applicable
     * </ul></p>
     *
     * @return The {@code String} representing the current mode of the {@code UIButton}
     */
    @Override
    public String getMode() {
        if (!isEnabled()) {
            return DISABLED_MODE;
        } else if (down) {
            return DOWN_MODE;
        } else if (interactionListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    /**
     * Called when the {@code UIButton} is pressed to activate all the listeners
     */
    private void activate() {
        for (ActivateEventListener listener : listeners) {
            listener.onActivated(this);
        }
    }

    /**
     * Binds the text to be shown on the {@code UIButton}
     *
     * @param binding The {@code Binding} containing the text
     */
    public void bindText(Binding<String> binding) {
        this.text = binding;
    }

    /**
     * Retrieves the text being shown on the {@code UIButton}
     *
     * @return The text shown on the {@code UIButton}
     */
    public String getText() {
        return text.get();
    }

    /**
     * Sets the text to be shown on the {@code UIButton}
     *
     * @param text The text to be shown on the {@code UIButton}
     */
    public void setText(String text) {
        this.text.set(text);
    }

    /**
     * Binds the image to be shown on the {@code UIButton}
     *
     * @param binding The {@code Binding} containing the {@code TextureRegion} corresponding to the image
     */
    public void bindImage(Binding<TextureRegion> binding) {
        this.image = binding;
    }

    /**
     * Sets the image to be shown on the {@code UIButton}
     *
     * @param image The {@code TextureRegion} corresponding to the image
     */
    public void setImage(TextureRegion image) {
        this.image.set(image);
    }

    /**
     * Retrieves the the image shown on the {@code UIButton}
     *
     * @return The {@code TextureRegion} corresponding to the image
     */
    public TextureRegion getImage() {
        return image.get();
    }

    /**
     * Binds the click sound to be played when the {@code UIButton} is clicked
     *
     * @param binding The {@code Binding} containing the {@code StaticSound} corresponding to the click sound
     */
    public void bindClickSound(Binding<StaticSound> binding) {
        clickSound = binding;
    }

    /**
     * Retrieves the click sound to be played when the {@code UIButton} is clicked
     *
     * @return The {@code StaticSound} corresponding to the click sound
     */
    public StaticSound getClickSound() {
        return clickSound.get();
    }

    /**
     * Sets the click sound to be played when the {@code UIButton} is clicked
     *
     * @param val The {@code StaticSound} corresponding to the click sound
     */
    public void setClickSound(StaticSound val) {
        clickSound.set(val);
    }

    /**
     * Binds the volume of the click sound
     *
     * @param binding The {@code Binding} containing the float representing volume the click sound
     */
    public void bindClickVolume(Binding<Float> binding) {
        clickVolume = binding;
    }

    /**
     * Retrieves the volume of the click sound
     *
     * @return The float representing the volume of the click sound
     */
    public float getClickVolume() {
        return clickVolume.get();
    }

    /**
     * Sets the volume of the click sound
     *
     * @param val The float representing the volume of the click sound
     */
    public void setClickVolume(float val) {
        clickVolume.set(val);
    }

    /**
     * Subscribes a listener that is called whenever the {@code UIButton} is activated
     *
     * @param listener The {@link ActivateEventListener} to be subscribed
     */
    public void subscribe(ActivateEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Unsubscribes a listener from the {@code UIButton}
     *
     * @param listener The {@code ActivateEventListener}to be unsubscribed
     */
    public void unsubscribe(ActivateEventListener listener) {
        listeners.remove(listener);
    }
}
