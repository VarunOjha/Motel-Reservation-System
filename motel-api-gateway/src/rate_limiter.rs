use std::time::{Duration, Instant};
use dashmap::DashMap;
use std::sync::Arc;

#[derive(Clone)]
pub struct RateLimiter {
    limits: Arc<DashMap<String, (u32, Instant)>>,
    duration: Duration,
}

impl RateLimiter {
    pub fn new(duration: Duration) -> Self {
        Self {
            limits: Arc::new(DashMap::new()),
            duration,
        }
    }

    pub fn check(&self, key: &str, limit: u32) -> bool {
        let now = Instant::now();
        let mut entry = self.limits.entry(key.to_string()).or_insert((0, now));

        if now.duration_since(entry.1) > self.duration {
            *entry = (1, now);
            true
        } else if entry.0 < limit {
            entry.0 += 1;
            true
        } else {
            false
        }
    }

    // pub fn reset(&self, key: &str) {
    //     self.limits.remove(key);
    // }

    // // Example usage of the reset method
    // pub fn reset_all(&self) {
    //     self.limits.clear();
    // }
}
