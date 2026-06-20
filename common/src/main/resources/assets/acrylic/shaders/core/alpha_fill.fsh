#version 330

out vec4 fragColor;

void main() {
    // Only the alpha channel is written (the pipeline restricts the color
    // write mask to WRITE_ALPHA), forcing the framebuffer fully opaque while
    // leaving the rendered RGB scene untouched.
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
