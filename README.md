# Java Networking Coursework

A collection of two related but distinct Java networking exercises. Both are retained in separate directories so the original implementations remain easy to compare.

## Projects

| Directory | Overview |
|---|---|
| `ПП/` | TCP/socket chat with client and server programs, message types, protocol handling, and command-line launch scripts. |
| `ПП2/` | An extended version that adds Java RMI interfaces and client/server implementations alongside the socket-based code. |

## Requirements

- JDK 8 or newer is the baseline documented by the project-specific macOS guide.
- A terminal or Java IDE.
- For network use, run the server first and connect clients to the selected host and port.

## Build and run

The repository includes platform-specific `.cmd` and `.sh` scripts. Start with the instructions in `ПП/README_MACOS.md` for macOS, and inspect the matching scripts in the target folder before running them. The RMI exercise has separate launch scripts and policy files.

No compile or runtime test was performed during this documentation update, so commands and firewall/network behavior should be verified on the target OS.

## Scope and limitations

These are educational examples of sockets, message protocols, client/server lifecycle, and RMI callbacks—not production-ready chat infrastructure. Authentication, threat modeling, and multi-user deployment guarantees should not be assumed. Confirm course and redistribution rules before making the collection public.