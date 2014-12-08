#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;
layout (location = 4) in vec3 aColor;

uniform mat4 model;
uniform mat4 modelViewProjection;
uniform mat4 shadowMatrix;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;
out vec3 vColor;

out float vDistance;

out vec4 vShadowCoords;

void main()
{
	vPosition = aPosition;
	vTexCoord = aTexCoord * 2.0;
	vNormal = normalize(aNormal);
	vTangent = aTangent.xyz;
	vBinormal = cross(aNormal, aTangent.xyz) * aTangent.w;
	vColor = aColor;
	
	vDistance = abs(distance(model * aPosition, vec4(0.0))) * 0.00143;
	vDistance = clamp(vDistance, 0.0, 1.0);
	
	vShadowCoords = shadowMatrix * (model * aPosition);

	gl_Position = modelViewProjection * aPosition;
}