#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;
layout (location = 4) in vec3 aColor;

uniform mat4 uCurrentModelMatrix;
uniform mat4 uCurrentModelViewMatrix;
uniform mat4 uPreviousModelViewMatrix;
uniform mat4 uModelViewProjectionMatrix;
uniform mat4 uShadowMatrix;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec4 vTangent;
out vec3 vColor;

out float vDepth;
out vec3 vEye;

out vec3 displacement;

out vec4 vShadowCoords;

out vec4 viewProjPosition;
out vec4 viewProjPositionD;

void main()
{
    vPosition = uModelViewProjectionMatrix * aPosition;
    vTexCoord = aTexCoord;
    vNormal = (uCurrentModelViewMatrix * vec4(aNormal, 0.0)).xyz;
    vTangent = aTangent;
	vColor = aColor;
	
	vec4 viewPosition = uCurrentModelViewMatrix * aPosition;
	vEye = normalize(viewPosition.xyz);
	vDepth = distance(viewPosition, vec4(0.0));// / 500.0;
	
	vec4 prevViewPosition = uPreviousModelViewMatrix * aPosition;
	displacement = viewPosition.xyz - prevViewPosition.xyz;
	displacement = displacement * (1.0 - vDepth);
	
	vShadowCoords = uShadowMatrix * (uCurrentModelMatrix * aPosition);
	
	gl_Position = vPosition;
}