#version 300 es
precision mediump float;

uniform sampler2D diffuseSampler;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

out vec4 fragColor;

void main()
{
	fragColor = vec4(vNormal, 1.0);
}
