#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;
layout (location = 4) in vec3 aColor;

uniform mat4 uModelMatrix;
uniform mat4 uModelViewMatrix;
uniform mat4 uModelViewProjectionMatrix;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec4 vTangent;
out vec3 vColor;

out float vDepth;
out vec3 vEye;

void main()
{
    vPosition = uModelViewProjectionMatrix * aPosition;
    vTexCoord = aTexCoord;
	vNormal = (uModelViewMatrix * vec4(aNormal, 0.0)).xyz;
    vTangent = uModelMatrix * aTangent;
	vColor = aColor;
	
	vec4 viewPosition = uModelViewMatrix * aPosition;
	vEye = normalize(viewPosition.xyz);
	vDepth = distance(viewPosition, vec4(0.0)) / 500.0f;
	
	gl_Position = vPosition;
}