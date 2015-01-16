#version 300 es

layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec4 aTangent;

layout (std140) uniform rockProperties
{
	vec4 properties[512];
};

uniform mat4 viewProjection;

out vec2 vTexCoord;
out vec3 vNormal;
out vec3 vTangent;
out vec3 vBinormal;
out float vDistance;
out vec3 vLight;

const vec3 lightPos = vec3(0.0, 50.0, 0.0);

void main()
{
	// get property for current instance
	vec4 property = properties[gl_InstanceID];
	
	// calculate final position
	vec4 position = aPosition * property.z;
	position.x += property.x;
	position.z += property.y;
	position.w = 1.0;
	
	// calculate shadow coords
	//vec4 shadowCoords = shadowMatrix * (position);
	
	// calculate shadows
	//vShadows = textureProj(shadowMapSampler, shadowCoords);
	
	// calculate final distance
	vDistance = property.w * property.w;
	
	// calculate attributes
	vTexCoord = aTexCoord;
	vNormal = aNormal;
	vTangent = aTangent.xyz;
	vBinormal = normalize(cross(aNormal, aTangent.xyz)) * aTangent.w;
	
	vec4 finalPosition = viewProjection * position;
	vLight = normalize(lightPos - position.xyz);
	
	// vertex shader output position
	gl_Position = finalPosition; //viewProjection * position;
}