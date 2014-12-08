#version 300 es
precision mediump float;

//uniform sampler2D diffuseSampler;
//uniform sampler2D normalSampler;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec3 vTangent;
in vec3 vBinormal;

out vec4 fragColor;

const vec3 vLight = normalize(vec3(1.0, 1.0, 0.0));

void main()
{
	float diffuse = dot(vNormal, vLight);
	diffuse = clamp(diffuse, 0.0, 1.0);
	
	fragColor = vec4(0.0, diffuse, 0.0, 1.0);

	/*vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	if(diffuseTex.w < 0.5) discard;

	vec4 normalTex = texture(normalSampler, vTexCoord);
	normalTex = normalTex * 2.0 - 1.0;
	vec3 normal = vTangent * normalTex.x + vBinormal * normalTex.y + vNormal * normalTex.z;
	
	float diffuse = clamp(dot(normal, vLight), 0.0, 1.0);*/
	
	
	/*if(gl_FrontFacing)
	{
		fragColor = vec4(diffuseTex.xyz * diffuse, 1.0);
	}
	else
	{
		fragColor = vec4(diffuseTex.xyz * (normalTex.w + 0.5), 1.0);
	}*/
	
	/*diffuse = clamp(dot(vNormal, vLight), 0.0, 1.0);
	fragColor = vec4(diffuseTex.xyz * (diffuse + 0.1), 1.0);*/
	
	//if(fragColor.w == 0.0) discard;/}
	//fragColor = vec4(vTexCoord, 0.0, 1.0);
}