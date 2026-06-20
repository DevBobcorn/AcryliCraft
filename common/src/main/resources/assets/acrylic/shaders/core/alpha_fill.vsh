#version 330

in vec3 Position;

void main() {
    // Position is already supplied in normalized device coordinates,
    // so the full-screen triangle pair is emitted without any transform.
    gl_Position = vec4(Position, 1.0);
}
