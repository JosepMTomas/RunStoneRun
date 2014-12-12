#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

uniform sampler2DShadow shadowMapSampler;

uniform mat4 model;
uniform mat4 modelViewProjection;
uniform mat4 shadowMatrix;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;

//out float vDiffuse;
out float vShadows;


vec4 vShadowCoords = shadowMatrix * (model * aPosition);

void main()
{	
	vPosition = aPosition;
	vTexCoord = aTexCoord;
	vNormal = normalize((model * vec4(aNormal, 0.0)).xyz);
	vTangent = aTangent.xyz;
	vBinormal = cross(vNormal, aTangent.xyz) * aTangent.z;
	
	/*vDiffuse = dot(vNormal, vLight);
	vDiffuse = clamp(vDiffuse, 0.0, 1.0);*/
	
	vShadows = textureProj(shadowMapSampler, vShadowCoords);
	//vShadows += 0.25;
	//vShadows = clamp(vShadows, 0.0, 1.0);
	
	gl_Position = modelViewProjection * aPosition;
}