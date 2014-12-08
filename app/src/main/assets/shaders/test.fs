#version 300 es
precision mediump float;

layout (location = 0) out vec4 fragNormals;
layout (location = 1) out vec4 fragDiffuse;
layout (location = 2) out vec4 fragSpecular;
layout (location = 3) out vec4 fragShadows;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec4 vTangent;
in vec3 vColor;

in float vDepth;
in vec3 vEye;

void main()
{
	fragNormals = vec4(vNormal, 1.0);
	fragDiffuse = vec4(1.0, 0.0, 0.0, 1.0);
	//fragSpecular = vec4(0.0, 0.0, 0.0, 1.0);
	fragSpecular = vec4(vDepth, 0.0, 0.0, 0.0);
	//fragShadows = vec4(0.5, 0.5, 0.5, 1.0);
	//fragShadows = vec4(vEye, 0.0);
	fragShadows = vec4(0.0);
}
