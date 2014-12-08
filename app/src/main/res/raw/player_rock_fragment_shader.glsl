#version 300 es
precision highp float;

//uniform sampler2D u_NormalTexture;
uniform samplerCube uCubeMap;
uniform vec3 eyePosition;

//uniform vec3 u_EyePosition;

in vec4 worldPosition;
in vec4 vPosition;
in vec2 vTexCoord;
in vec3 vNormal;
in vec4 vTangent;

out vec4 fragColor;

layout (location = 0) out vec4 framebufferOut;

void main()
{
	/*vec3 binormal = normalize(cross(vNormal, vTangent.xyz));// * v_Tangent.w;
	vec4 normalTex = texture(u_NormalTexture, vTexCoord * vec2(3.0));
	normalTex = normalize(normalTex * 2.0 - 1.0);*/
	
	//normal = intersection.tangent*normal.x + binormal*normal.y + intersection.normal*normal.z;
	/*vec3 normal = vTangent.xyz * normalTex.x + binormal * -normalTex.y + vNormal * normalTex.z;
	
	float diffuse = max(dot(normal, vec3(0,1,0)), 0.0);
	float ambient = max(dot(normal, vec3(0,-1,0)), 0.0);
	ambient = ambient * 0.5;
	
	vec3 color = vec3(diffuse);
	//color = color + (vec3(ambient) * vec3(1.0,0.7,0.2));
	color = color + (vec3(ambient) * vec3(0.2,1.0,0.2));
	
	//vec3 vEye = normalize(v_Position.xyz - u_EyePosition);
	//diffuse = dot(normal, vEye);*/
	
	//fragColor = vec4(vNormal, 1.0);
	/*fragColor = vec4(vTexCoord, 0.0, 1.0);
	framebufferOut = vec4(vTexCoord, 0.0, 1.0);*/
	
	vec3 V = normalize(worldPosition.xyz - eyePosition);
	//fragColor = texture(uCubeMap, reflect(V, vNormal));
	//vec3 V = normalize(eyePosition - worldPosition.xyz);
	vec3 R = normalize(vNormal * 2.0 * dot(vNormal,V) - V);
	//fragColor = texture(uCubeMap, R);
	
	fragColor = vec4(vTexCoord, 0.0, 0.0);
	
	//fragColor = mix(vec4(vTexCoord, 0.0, 1.0), texture(uCubeMap, R), 0.5);
	//fragColor = vec4(R, 1.0);
	
	//gl_FragColor = vec4(normal, 0.0);
	//gl_FragColor = vec4(color, 0.0);
	//gl_FragColor = vec4(normal, 0.0);
	//gl_FragColor = v_Tangent;
}