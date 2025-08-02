use hyper::{Client, Request, Response, Body, Uri};

use crate::config::RouteConfig;

pub async fn proxy(req: Request<Body>, route: &RouteConfig) -> Result<Response<Body>, hyper::Error> {
    let client = Client::new();
    let uri_string = format!("{}{}", route.upstream, req.uri().path());

    let uri: Uri = match uri_string.parse() {
        Ok(parsed_uri) => parsed_uri,
        Err(_) => {
            return Ok(Response::builder()
                .status(400)
                .body(Body::from("Invalid URI"))
                .unwrap());
        }
    };

    let method = req.method().clone(); // Clone the method before moving `req`
    let headers = req.headers().clone(); // Clone the headers before moving `req`

    let mut new_req = match Request::builder()
        .method(method)
        .uri(uri)
        .body(req.into_body()) // Move `req` here
    {
        Ok(request) => request,
        Err(_) => {
            return Ok(Response::builder()
                .status(500)
                .body(Body::from("Failed to build request"))
                .unwrap());
        }
    };

    *new_req.headers_mut() = headers; // Use cloned headers
    client.request(new_req).await
}
