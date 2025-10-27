import { NextRequest } from "next/server";

export async function POST(request: NextRequest) {
    const body = await request.json();
    const apiKey: string = process.env.NEXT_PRIVATE_CHAT_API_KEY || "";

    const response = await fetch("http://localhost:8080/api/chat", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-API-KEY": apiKey,
        },
        body: JSON.stringify(body),
    });

    return new Response(response.body, {
        status: response.status,
        headers: response.headers,
    });
}
