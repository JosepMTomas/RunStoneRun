#version 300 es
precision lowp float;

uniform sampler2D diffuseSampler;

//DEBUG
uniform int lod;

in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;

in float vDistance;
in vec4 vDiffuse;
in vec4 vAmbient;

out vec4 fragColor;

void main()
{	
	lowp vec4 diffuseTex = texture(diffuseSampler, vTexCoord);
	
	if(diffuseTex.w < 0.6) discard;

	fragColor = (diffuseTex * vDiffuse);
	//fragColor = fragColor * vShadows;
	
	fragColor += (diffuseTex * vAmbient);
	
	fragColor = mix(fragColor, vec4(1.0), vDistance);
	
	/*if(lod == 0)
	{
		fragColor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else
	{
		fragColor = vec4(0.0, 0.0, 1.0, 1.0);
	}*/
	
	/*switch(index)
	{
	case 0:
		fragColor = vec4(1.0,0.0,0.0,1.0);
		break;
	case 1:
		fragColor = vec4(1.0,1.0,0.0,1.0);
		break;
	case 2:
		fragColor = vec4(0.0,1.0,0.0,1.0);
		break;
	case 3:
		fragColor = vec4(1.0,0.0,1.0,1.0);
		break;
	case 4:
		fragColor = vec4(0.75,0.75,0.75,1.0);
		break;
	case 5:
		fragColor = vec4(0.0,1.0,1.0,1.0);
		break;
	case 6:
		fragColor = vec4(0.0,0.0,1.0,1.0);
		break;
	case 7:
		fragColor = vec4(0.5,0.5,0.5,1.0);
		break;
	case 8:
		fragColor = vec4(1.0,0.5,0.0,1.0);
		break;
	default:
		fragColor = vec4(0.0);
		break;
	}*/

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