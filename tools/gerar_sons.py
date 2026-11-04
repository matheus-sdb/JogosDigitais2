"""Gera os sons do TrioTiles em assets/sounds/ (WAV, 44.1 kHz, 16 bits, mono).

Os sons são sintetizados aqui mesmo: não dependem de arquivos baixados nem de licença.
Uso (na raiz do projeto):  python tools/gerar_sons.py
"""
import math
import os
import struct
import wave

RATE = 44100
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "assets", "sounds")


def write(name, samples):
    path = os.path.join(OUT, name)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1.0, min(1.0, s)) * 32767)) for s in samples))
    print("gerado:", os.path.normpath(path))


def piano(freq, seconds=0.45, volume=0.55):
    """Nota parecida com piano: fundamental + harmônicos, ataque rápido e decaimento."""
    n = int(RATE * seconds)
    out = []
    for i in range(n):
        t = i / RATE
        attack = min(1.0, t / 0.005)
        decay = math.exp(-t * 7.0)
        s = (math.sin(2 * math.pi * freq * t)
             + 0.45 * math.sin(2 * math.pi * 2 * freq * t) * math.exp(-t * 4)
             + 0.20 * math.sin(2 * math.pi * 3 * freq * t) * math.exp(-t * 6))
        out.append(volume * attack * decay * s / 1.65)
    return fade_out(out)


def bonk(seconds=0.22, volume=0.45):
    """Erro: som grave e curto que desce de tom. Avisa sem punir."""
    n = int(RATE * seconds)
    out, phase = [], 0.0
    for i in range(n):
        t = i / RATE
        freq = 220 * math.exp(-t * 4.0)
        phase += 2 * math.pi * freq / RATE
        out.append(volume * math.exp(-t * 14) * math.sin(phase))
    return fade_out(out)


def tick(freq=1400, seconds=0.045, volume=0.35):
    n = int(RATE * seconds)
    return fade_out([volume * math.exp(-i / RATE * 60) * math.sin(2 * math.pi * freq * i / RATE) for i in range(n)])


def confirm(volume=0.4):
    return piano(659.25, 0.12, volume)[: int(RATE * 0.09)] + piano(987.77, 0.25, volume)


def arpeggio(volume=0.45):
    """Giro do knob acertado: Dó-Mi-Sol bem rápido, como um 'brilho'."""
    step = int(RATE * 0.06)
    out = []
    for f in (523.25, 659.25, 783.99):
        out += piano(f, 0.3, volume)[:step]
    return out + piano(1046.5, 0.35, volume)


def fade_out(samples, ms=8):
    k = min(len(samples), int(RATE * ms / 1000))
    for i in range(k):
        samples[-1 - i] *= i / k
    return samples


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    # Trilhas 1, 2, 3 = Dó, Mi, Sol (acorde de Dó maior): acertar em sequência "toca música".
    write("trilha1.wav", piano(523.25))
    write("trilha2.wav", piano(659.25))
    write("trilha3.wav", piano(783.99))
    write("erro.wav", bonk())
    write("menu_mover.wav", tick())
    write("menu_confirmar.wav", confirm())
    write("giro.wav", arpeggio())
