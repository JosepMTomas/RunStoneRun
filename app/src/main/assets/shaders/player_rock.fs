#version 300 es
precision highp float;

layout (std140) uniform lightInfo
{
	vec3 vLight;
	vec4 lightColor;
	vec4 ambientColor;
};

// TODO: textures

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

//in float vDiffuse;
in float vShadows;

out vec4 fragColor;

void main()
{
	fragColor = vec4(vTexCoord, 0.0, 1.0);
	//fragColor = vec4(vNormal, 1.0);
	
	vec4 ambient = fragColor * ambientColor;
	
	float vDiffuse = dot(vNormal, vLight);
	vDiffuse = clamp(vDiffuse, 0.0, 1.0);
	
	float shading = vDiffuse * vShadows;
	//shading += 0.25;
	shading = clamp(shading, 0.0, 1.0);
	
	fragColor *= shading;
	fragColor += ambient;
}