#version 300 es
precision mediump float;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

out vec4 fragColor;

void main()
{
	//fragColor = vec4(vNormal, 1.0);
	fragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
