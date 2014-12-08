#version 300 es
precision highp float;

uniform sampler2D u_GrassColorTexture;
uniform sampler2D u_GrassNormalTexture;
uniform sampler2D u_GroundColorTexture;
uniform sampler2D u_GroundAlphaTexture;
uniform sampler2D u_GroundNormalTexture;

uniform sampler2DShadow shadowMap;
vec3 light_position = vec3(100.0, 100.0, 0.0);

uniform float u_Time;

in vec4 v_Position;
in vec2 v_TexCoord;
in vec3 v_Normal;
in vec4 v_Tangent;
in vec4 v_Color;

in vec3 vEyeSpaceNormal;
in vec3 vEyeSpacePosition;
in vec4 vShadowCoords;

out vec4 fragColor;

void main()
{
	vec2 texCoord = v_TexCoord * 2.0;
	vec3 binormal = normalize(cross(v_Normal, v_Tangent.xyz));

	vec4 grassColor = texture(u_GrassColorTexture, texCoord * 2.0);
	vec4 grassNormal = texture(u_GrassNormalTexture, texCoord * 2.0);
	vec4 groundColor = texture(u_GroundColorTexture, texCoord);
	vec4 groundAlpha = texture(u_GroundAlphaTexture, texCoord);
	vec4 groundNormal = texture(u_GroundNormalTexture, texCoord);
	
	grassNormal  = normalize(grassNormal * 2.0 - 1.0);
	groundNormal = normalize(groundNormal * 2.0 - 1.0);
	
	//vec4 normalMix = mix(grassNormal, groundNormal, v_Color.x);
	//vec3 normal = v_Tangent.xyz * normalMix.x + binormal * -normalMix.y + v_Normal * normalMix.z;
	
	//gl_FragColor = mix(vec4(0.0,0.8,0.0,1.0), vec4(0.3,0.5,0.0,1.0), v_Color.x);
	//gl_FragColor = mix(vec4(1.0), vec4(0.0), v_Color.x);
	
	float wetness = v_Color.x * 2.0;
	//float wetness = 7.0;
	//wetness = wetness * ((sin(u_Time) + 1.0) / 2.0);
	float alpha2 = pow(v_Color.x, 1.0);
	//float alpha = pow(groundAlpha.x, v_Color.y * 3.0) * (wetness);
	//alpha = clamp(alpha, 0.0, 1.0);
	
	float alpha = (groundAlpha.x + 0.1) * wetness;
	alpha = pow(alpha, 2.0);
	alpha = alpha * 2.0;
	alpha = clamp(alpha, 0.0, 1.0);
	
	vec4 normalMix = mix(groundNormal, vec4(0,0,1,0), alpha);
	vec3 normal = v_Tangent.xyz * normalMix.x + binormal * -normalMix.y + v_Normal * normalMix.z;
	
	vec4 wetGround = groundColor * 0.25;
	//vec4 diffuseMix = mix(groundColor, wetGround, alpha);
	vec4 diffuseMix = mix(grassColor, wetGround, alpha);
	float diffuse = max(dot(normal, vec3(1,1,0)), 0.0);
	
	
	//gl_FragColor = vec4(diffuseMix * diffuse, 1.0);
	//gl_FragColor = diffuseMix;
	
	diffuseMix = diffuseMix * diffuse;
	//fragColor = vec4(pow(v_Color.x,2.0));
	//fragColor = vec4(1.0);
	
	//gl_FragColor = vec4(alpha);
	//gl_FragColor = vec4(v_Color.x);
	
	//alpha = pow(groundAlpha.x, v_Color.x * 3.0) * (v_Color.x * 4.0);
	
	//gl_FragColor = vec4(v_Color.x);
	
	//fragColor = vec4(diffuse);
	
	vec3 L = light_position - vEyeSpacePosition;
	float d = length(L);
	L = normalize(L);
	
	if(vShadowCoords.w > 1.0)
	{
		float shadow = textureProj(shadowMap, vShadowCoords);
		fragColor = mix(vec4(diffuseMix), vec4(diffuseMix)*shadow, 0.75);
	}
	else
	{
		fragColor = vec4(diffuseMix);
	}
}