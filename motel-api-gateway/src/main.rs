mod config;
mod proxy;
mod rate_limiter;

use hyper::{
    service::{make_service_fn, service_fn},
    Body, Request, Response, Error, Server,
};
use crate::config::GatewayConfig;
use crate::rate_limiter::RateLimiter;
use std::sync::Arc;
use std::time::Duration;

#[tokio::main]
async fn main() {
    let config = match GatewayConfig::load_from_file("routes.yaml") {
        Ok(cfg) => cfg,
        Err(err) => {
            eprintln!("Failed to vo load configuration: {}", err);
            std::process::exit(1);
        }
    };

    let limiter = Arc::new(RateLimiter::new(Duration::from_secs(60))); // Set rate-limiting duration

    let make_svc = make_service_fn(move |_| {
        let config = config.clone(); // Use derived Clone trait
        let limiter = limiter.clone();

        async move {
            Ok::<_, Error>(service_fn(move |req: Request<Body>| {
                let config = config.clone(); // Use derived Clone trait
                let limiter = limiter.clone();

                async move {
                    for route in &config.routes {
                        if req.uri().path().starts_with(&route.path) {
                            let key = req
                                .headers()
                                .get("x-forwarded-for")
                                .and_then(|v| v.to_str().ok())
                                .unwrap_or("unknown");

                            let rate_limit_key = format!("{}:{}", key, route.path);
                            if limiter.check(&rate_limit_key, route.rate_limit) {
                                return proxy::proxy(req, route).await;
                            } else {
                                let json_response = r#"{"response": {"http_code": "429", "message": "too many requests"}}"#;
                                return Ok(Response::builder()
                                    .status(429)
                                    .header("content-type", "application/json")
                                    .body(Body::from(json_response))
                                    .unwrap());
                            }
                        }
                    }

                    Ok(Response::builder()
                        .status(404)
                        .body(Body::from("Not found"))
                        .unwrap())
                }
            }))
        }
    });

    let addr = ([0, 0, 0, 0], 8080).into();
    println!("API Gateway listening on http://{}", addr);
    if let Err(e) = Server::bind(&addr).serve(make_svc).await {
        eprintln!("Server error: {}", e);
    }
}
