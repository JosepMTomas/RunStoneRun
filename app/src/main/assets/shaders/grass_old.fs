#version 300 es
precision highp float;

uniform sampler2D diffuseSampler;
uniform int lod;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

in float vDistance;

in float vShadows;
//in vec4 vShadowCoords;

out vec4 fragColor;

void main()
{
	fragColor = texture(diffuseSampler, vTexCoord);
	
	/*float diffuse = dot(vNormal, vec3(1.0,0.0,1.0));
	diffuse = abs(diffuse);
	
	fragColor = fragColor * vec4(diffuse,diffuse,diffuse,1.0);*/
	
	if(fragColor.w < 0.25) discard;
	
	fragColor *= vShadows;
	
	fragColor = mix(fragColor, vec4(1.0), vDistance);
	//fragColor = mix(vec4(0.0,0.7,0.0,1.0), vec4(1.0), vDistance);
	//fragColor = vec4(vDistance, vDistance, vDistance, 1.0);
	
	/*if(vShadowCoords.w > 1.0)
	{
		float shadow = textureProj(shadowMapSampler, vShadowCoords);
		shadow += 0.5;
		fragColor *= vec4(vec3(shadow), 1.0);
	}*/
	
	/*if(lod == 0)
	{
		fragColor = vec4(0.0, 1.0, 0.0, 1.0);
	}
	else if(lod == 1)
	{
		fragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else
	{
		fragColor = vec4(1.0, 0.0, 1.0, 1.0);
	}*/
}
