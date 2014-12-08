#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout (std140) uniform modelMatrices
{
    mat4 model[128];
};

uniform mat4 worldModel;
uniform mat4 viewProjection;
uniform mat4 shadowMatrix;

uniform sampler2DShadow shadowMapSampler;

out vec4 vPosition;
out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;

out float vDistance;

out float vShadows;
//out vec4 vShadowCoords;

mat4 grassModel = worldModel * model[gl_InstanceID];
mat4 modelViewProjection = viewProjection * grassModel;
vec4 vShadowCoords = shadowMatrix * (grassModel * aPosition);

void main()
{
	//mat4 modelViewProjection = viewProjection;
	
	vPosition = modelViewProjection * aPosition;
	vTexCoord = aTexCoord * vec2(1.0, -1.0);
	vNormal = (modelViewProjection * vec4(aNormal, 0.0)).xyz;
	vTangent = (modelViewProjection * vec4(aTangent.xyz, 0.0)).xyz;
	vBinormal = cross(aNormal, aTangent.xyz) * aTangent.w;
	
	vDistance = abs(distance(grassModel * aPosition, vec4(0.0))) * 0.00125;
	vDistance = pow(vDistance, 2.0);
	vDistance = clamp(vDistance, 0.0, 1.0);
	
	/*if(vShadowCoords.w > 1.0)
	{
		vShadows = textureProj(shadowMapSampler, vShadowCoords);
		vShadows += 0.5;
	}
	else
	{
		vShadows = 1.0;
	}*/
	vShadows = textureProj(shadowMapSampler, vShadowCoords);
	vShadows += 0.25;
	vShadows = clamp(vShadows, 0.0, 1.0);
	
	gl_Position = vPosition;
}