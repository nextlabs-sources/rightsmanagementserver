package com.nextlabs.kms.context;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.spring.SpringFinder;
import org.restlet.ext.spring.SpringRouter;
import org.restlet.resource.ServerResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nextlabs.kms.controller.service.*;

@Configuration
public class RestletServiceContext {
	@Bean
	public Restlet root() {
		final SpringRouter router = new SpringRouter(restletContext());
		
		router.attach("/service/createKeyRing", new SpringFinder(){
			@Override
			public ServerResource create(){
				return createKeyRingResource();
			}
		});
		
		router.attach("/service/deleteKeyRing", new SpringFinder(){
			@Override
			public ServerResource create(){
				return deleteKeyRingResource();
			}
		});
		
		router.attach("/service/disableKeyRing", new SpringFinder(){
			@Override
			public ServerResource create(){
				return disableKeyRingResource();
			}
		});
		
		router.attach("/service/enableKeyRing", new SpringFinder(){
			@Override
			public ServerResource create(){
				return enableKeyRingResource();
			}
		});
		
		router.attach("/service/generateKey", new SpringFinder(){
			@Override
			public ServerResource create(){
				return generateKeyResource();
			}
		});
		
		router.attach("/service/getAllKeyRingsWithKeys", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getAllKeyRingsWithKeysResource();
			}
		});
		
		router.attach("/service/getKey", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getKeyResource();
			}
		});
		
		router.attach("/service/getKeyRingNames", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getKeyRingNamesResource();
			}
		});
		
		router.attach("/service/getKeyRing", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getKeyRingResource();
			}
		});
		
		router.attach("/service/getKeyRingWithKeys", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getKeyRingWithKeysResource();
			}
		});
		
		router.attach("/service/getKeyRings", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getKeyRingsResource();
			}
		});
		
		router.attach("/service/getLatestKey", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getLatestKeyResource();
			}
		});
		
		router.attach("/service/getTenantDetail", new SpringFinder(){
			@Override
			public ServerResource create(){
				return getTenantDetailResource();
			}
		});
		
		router.attach("/service/importKeyRing", new SpringFinder(){
			@Override
			public ServerResource create(){
				return importKeyRingResource();
			}
		});
		
		router.attach("/service/registerClient", new SpringFinder(){
			@Override
			public ServerResource create(){
				return registerClientResource();
			}
		});
		
		router.attach("/service/registerTenant", new SpringFinder(){
			@Override
			public ServerResource create(){
				return registerTenantResource();
			}
		});
		return router;
	}

	@Bean
	public CreateKeyRingResource createKeyRingResource(){
		return new CreateKeyRingResource();
	}
	
	@Bean
	public DeleteKeyRingResource deleteKeyRingResource(){
		return new DeleteKeyRingResource();
	}
	
	@Bean
	public DisableKeyRingResource disableKeyRingResource(){
		return new DisableKeyRingResource();
	}
	
	@Bean
	public EnableKeyRingResource enableKeyRingResource(){
		return new EnableKeyRingResource();
	}
	
	@Bean
	public GenerateKeyResource generateKeyResource(){
		return new GenerateKeyResource();
	}
	
	@Bean
	public GetAllKeyRingsWithKeysResource getAllKeyRingsWithKeysResource(){
		return new GetAllKeyRingsWithKeysResource();
	}
	
	@Bean
	public GetKeyResource getKeyResource(){
		return new GetKeyResource();
	}
	
	@Bean
	public GetKeyRingNamesResource getKeyRingNamesResource(){
		return new GetKeyRingNamesResource();
	}
	
	@Bean
	public GetKeyRingResource getKeyRingResource(){
		return new GetKeyRingResource();
	}
	
	@Bean
	public GetKeyRingWithKeysResource getKeyRingWithKeysResource(){
		return new GetKeyRingWithKeysResource();
	}
	
	@Bean
	public GetKeyRingsResource getKeyRingsResource(){
		return new GetKeyRingsResource();
	}
	
	@Bean
	public GetLatestKeyResource getLatestKeyResource(){
		return new GetLatestKeyResource();
	}
	
	@Bean
	public GetTenantDetailResource getTenantDetailResource(){
		return new GetTenantDetailResource();
	}
	
	@Bean
	public ImportKeyRingResource importKeyRingResource(){
		return new ImportKeyRingResource();
	}
	
	@Bean
	public RegisterClientResource registerClientResource(){
		return new RegisterClientResource();
	}
	
	@Bean
	public RegisterTenantResource registerTenantResource(){
		return new RegisterTenantResource();
	}
	
	@Bean
	public Context restletContext() {
		return new Context();
	}
}