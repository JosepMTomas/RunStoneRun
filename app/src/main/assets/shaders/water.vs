#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

uniform mat4 model;
uniform mat4 modelViewProjection;
uniform mat4 shadowMatrix;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec4 vTangent;
out vec3 vBinormal;

out float vDistance;

out vec4 vShadowCoords;

void main()
{
	vPosition = model * aPosition;
	vTexCoord = aTexCoord;
	vNormal = aNormal;
	vTangent = aTangent;
	vBinormal = cross(vNormal, vTangent.xyz) * vTangent.w;
	
	vDistance = abs(distance(model * aPosition, vec4(0.0))) * 0.00143;
	vDistance = clamp(vDistance, 0.0, 1.0);
	
	vShadowCoords = shadowMatrix * (model * aPosition);

	gl_Position = modelViewProjection * aPosition;
}