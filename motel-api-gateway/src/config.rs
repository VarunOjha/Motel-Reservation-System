use serde::Deserialize;
use std::fs;
use std::error::Error;

#[derive(Debug, Deserialize, Clone)]
pub struct RouteConfig {
    pub path: String,
    pub upstream: String,
    pub rate_limit: u32,
}

#[derive(Debug, Deserialize, Clone)] // Added Clone trait to GatewayConfig
pub struct GatewayConfig {
    pub routes: Vec<RouteConfig>,
}

impl GatewayConfig {
    pub fn load_from_file(path: &str) -> Result<Self, Box<dyn Error>> {
        let content = fs::read_to_string(path)?; // Read the file content
        let config: GatewayConfig = serde_yaml::from_str(&content)?; // Parse YAML into GatewayConfig
        Ok(config) // Return the parsed configuration
    }
}
