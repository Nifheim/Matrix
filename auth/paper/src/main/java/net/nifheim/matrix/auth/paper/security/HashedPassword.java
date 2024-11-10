package net.nifheim.matrix.auth.paper.security;

public record HashedPassword(int iterations, String hash, String salt) {

}
