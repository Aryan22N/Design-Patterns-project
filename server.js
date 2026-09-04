'use strict';
const express = require('express');
const path = require('path');
const { execSync } = require('child_process');
const fs = require('fs');

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.static(path.join(__dirname)));


const JAVA_SRC = path.join(__dirname, 'src');
const JAVA_OUT = path.join(__dirname, 'out');
const JAVA_MAIN = 'com.parking.client.Main';

function findJava() {
  const candidates = [
    'java',
    'C:\\Users\\Aryan\\.jdks\\openjdk-23.0.2\\bin\\java.exe',
    'C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-21\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-17\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk1.8.0_391\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.1.12-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.9.9-hotspot\\bin\\java.exe',
  ];
  for (const c of candidates) {
    try {
      execSync(`"${c}" -version`, { stdio: 'pipe' });
      return c;
    } catch (_) { }
  }
  return null;
}

function findJavac() {
  const candidates = [
    'javac',
    'C:\\Users\\Aryan\\.jdks\\openjdk-23.0.2\\bin\\javac.exe',
    'C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\javac.exe',
    'C:\\Program Files\\Java\\jdk-21\\bin\\javac.exe',
    'C:\\Program Files\\Java\\jdk-17\\bin\\javac.exe',
    'C:\\Program Files\\Java\\jdk1.8.0_391\\bin\\javac.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.1.12-hotspot\\bin\\javac.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.9.9-hotspot\\bin\\javac.exe',
  ];
  for (const c of candidates) {
    try {
      execSync(`"${c}" -version`, { stdio: 'pipe' });
      return c;
    } catch (_) { }
  }
  return null;
}

let JAVA = findJava() || 'java';
let JAVAC = findJavac() || 'javac';

const JAVA_LIB = path.join(__dirname, 'lib');
const CLASSPATH = fs.existsSync(JAVA_LIB) ? `"${JAVA_LIB}/*;${JAVA_OUT}"` : `"${JAVA_OUT}"`;

function compileJava() {
  if (!fs.existsSync(JAVA_OUT)) fs.mkdirSync(JAVA_OUT, { recursive: true });
  const srcs = walkJavaSrc(JAVA_SRC).join(' ');
  const cmd = `"${JAVAC}" -d "${JAVA_OUT}" -cp ${CLASSPATH} -sourcepath "${JAVA_SRC}" ${srcs}`;
  console.log('[Java] Compiling…');
  try {
    execSync(cmd, { stdio: 'pipe' });
    console.log('[Java] Compilation OK');
    return true;
  } catch (e) {
    console.error('[Java] Compilation FAILED:\n', e.stderr?.toString() || e.message);
    return false;
  }
}

function walkJavaSrc(dir) {
  const results = [];
  for (const f of fs.readdirSync(dir)) {
    const full = path.join(dir, f);
    if (fs.statSync(full).isDirectory()) results.push(...walkJavaSrc(full));
    else if (f.endsWith('.java')) results.push(`"${full}"`);
  }
  return results;
}

/** Run a Java command, return parsed JSON result. */
function runJava(args) {
  const cmd = `"${JAVA}" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp ${CLASSPATH} ${JAVA_MAIN} ${args.map(a => `"${a}"`).join(' ')}`;

  try {
    const raw = execSync(cmd, { stdio: 'pipe', timeout: 15000, encoding: 'utf8' }).trim();
    // Find the last line that looks like JSON
    const lines = raw.split('\n').filter(l => l.trim().startsWith('{'));
    const jsonLine = lines[lines.length - 1] || '{}';
    return JSON.parse(jsonLine);
  } catch (e) {
    const stderr = e.stderr?.toString() || '';
    const stdout = e.stdout?.toString() || '';
    // Try to parse stdout anyway
    const lines = stdout.split('\n').filter(l => l.trim().startsWith('{'));
    if (lines.length) {
      try { return JSON.parse(lines[lines.length - 1]); } catch (_) { }
    }
    throw new Error(stderr || e.message);
  }
}

// ─────────────────────────────────────────────────────────────
// In-memory state (demo — resets on restart)
// ─────────────────────────────────────────────────────────────

// Spot catalog matching Java Main.java SPOT_CATALOG
const SPOT_CATALOG = [
  { spotId: 'S-01', spotType: 'Small', zone: 'Premium', pricingMode: 'FlatRate', rate: '₹150/hr' },
  { spotId: 'S-02', spotType: 'Small', zone: 'Premium', pricingMode: 'FlatRate', rate: '₹150/hr' },
  { spotId: 'S-03', spotType: 'Small', zone: 'Regular', pricingMode: 'Hourly', rate: '₹50/hr' },
  { spotId: 'S-04', spotType: 'Small', zone: 'Regular', pricingMode: 'Hourly', rate: '₹50/hr' },
  { spotId: 'M-01', spotType: 'Medium', zone: 'Premium', pricingMode: 'FlatRate', rate: '₹250/hr' },
  { spotId: 'M-02', spotType: 'Medium', zone: 'Premium', pricingMode: 'FlatRate', rate: '₹250/hr' },
  { spotId: 'M-03', spotType: 'Medium', zone: 'Regular', pricingMode: 'Hourly', rate: '₹50/hr' },
  { spotId: 'M-04', spotType: 'Medium', zone: 'Regular', pricingMode: 'Hourly', rate: '₹50/hr' },
  { spotId: 'L-01', spotType: 'Large', zone: 'Premium', pricingMode: 'FlatRate', rate: '₹500/hr' },
  { spotId: 'L-02', spotType: 'Large', zone: 'Premium', pricingMode: 'FlatRate', rate: '₹500/hr' },
  { spotId: 'L-03', spotType: 'Large', zone: 'Regular', pricingMode: 'Hourly', rate: '₹50/hr' },
  { spotId: 'L-04', spotType: 'Large', zone: 'Regular', pricingMode: 'Hourly', rate: '₹50/hr' },
];

// spotId → { occupied: bool, ticketId: string|null }
const spotState = {};
SPOT_CATALOG.forEach(s => { spotState[s.spotId] = { occupied: false, ticketId: null }; });

// ticketId → full booking data
const activeSessions = {};

// Vehicle size rules (matches Java Vehicle.getSize())
function vehicleSize(vehicleType) {
  switch ((vehicleType || '').toLowerCase()) {
    case 'bike': return 1;
    case 'truck': return 3;
    default: return 2; // Car
  }
}

function spotEligible(spot, vSize, zone) {
  const type = spot.spotType.toLowerCase();
  let sizeOk = false;
  if (vSize === 1) sizeOk = type === 'small';
  else if (vSize === 2) sizeOk = type === 'medium';
  else sizeOk = type === 'large';
  const zoneOk = !zone || zone === 'any' || spot.zone.toLowerCase() === zone.toLowerCase();
  return sizeOk && zoneOk;
}

// ─────────────────────────────────────────────────────────────
// REST Endpoints
// ─────────────────────────────────────────────────────────────

/** GET /api/health */
app.get('/api/health', (req, res) => res.json({ status: 'ok', java: JAVA, javac: JAVAC }));

/**
 * GET /api/spots?vehicleType=Car&zone=Premium
 * Returns eligible spots with live availability from MySQL database via Java CLI.
 */
app.get('/api/spots', (req, res) => {
  const { vehicleType = 'Car', zone = 'any' } = req.query;
  try {
    const data = runJava(['QUERY_SPOTS', vehicleType, zone]);
    res.json(data);
  } catch (e) {
    res.status(500).json({ status: 'ERROR', message: e.message });
  }
});

/**
 * POST /api/book
 * Body: { vehicleType, plate, zone, spotId, spotType, paymentMethod }
 * Runs Java BOOK command, updates database & session state.
 */
app.post('/api/book', async (req, res) => {
  const { vehicleType, plate, zone, spotId, spotType, paymentMethod } = req.body;

  // Validate vehicle number format
  if (!plate || !/^[A-Z0-9 -]{4,15}$/i.test(plate.trim())) {
    return res.status(400).json({ status: 'ERROR', message: 'Invalid vehicle number format. Must be 4-15 alphanumeric characters (e.g. MH-31-AB-1234).' });
  }

  try {
    const result = runJava(['BOOK', vehicleType, plate, zone, spotId, spotType, paymentMethod || 'Card']);

    if (result.status === 'ERROR' || !result.ticketId) {
      return res.status(400).json(result.status ? result : { status: 'ERROR', message: result.message || 'Booking failed' });
    }

    // Save session
    activeSessions[result.ticketId] = {
      ticketId: result.ticketId,
      plate: result.plate || plate,
      vehicleType: result.vehicleType || vehicleType,
      zone: result.zone || zone,
      spotId: result.spotId || spotId,
      spotType: result.spotType || spotType,
      paymentMethod: paymentMethod || 'Card',
      pricingMode: result.pricingMode,
      rateDescription: result.rateDescription,
      entryTime: result.entryTime || new Date().toISOString(),
    };

    res.json(result);
  } catch (e) {
    res.status(500).json({ status: 'ERROR', message: e.message });
  }
});

/**
 * POST /api/exit
 * Body: { ticketId, hoursParked }
 * Computes fee via Java EXIT command.
 */
app.post('/api/exit', (req, res) => {
  const { ticketId, hoursParked } = req.body;
  const session = activeSessions[ticketId];
  if (!session) {
    return res.status(404).json({ status: 'ERROR', message: 'Session not found: ' + ticketId });
  }

  const hours = Math.max(1, Math.ceil(hoursParked || 1));

  try {
    const result = runJava([
      'EXIT', ticketId, session.plate, session.zone,
      session.spotId, session.spotType, String(hours),
    ]);
    res.json({ ...result, entryTime: session.entryTime });
  } catch (e) {
    res.status(500).json({ status: 'ERROR', message: e.message });
  }
});

/**
 * POST /api/pay
 * Body: { ticketId, paymentMethod, hoursParked }
 * Processes payment, releases spot.
 */
app.post('/api/pay', (req, res) => {
  const { ticketId, paymentMethod, hoursParked } = req.body;
  const session = activeSessions[ticketId];
  if (!session) {
    return res.status(404).json({ status: 'ERROR', message: 'Session not found: ' + ticketId });
  }

  const hours = Math.max(1, Math.ceil(hoursParked || 1));

  try {
    const result = runJava([
      'PAY', ticketId, session.plate, session.vehicleType, session.zone,
      session.spotId, session.spotType, String(hours), paymentMethod || session.paymentMethod || 'Card',
    ]);

    if (result.status === 'PAID') {
      // Release spot in server state so it becomes available for other vehicles
      const targetSpotId = result.spotId || session.spotId;
      if (targetSpotId && spotState[targetSpotId]) {
        spotState[targetSpotId] = { occupied: false, ticketId: null };
      }
      if (session.spotId && spotState[session.spotId]) {
        spotState[session.spotId] = { occupied: false, ticketId: null };
      }
      // Enrich with session data for receipt
      result.entryTime = session.entryTime;
      result.rateDescription = session.rateDescription;
      delete activeSessions[ticketId];
    }

    res.json(result);
  } catch (e) {
    res.status(500).json({ status: 'ERROR', message: e.message });
  }
});

// ─────────────────────────────────────────────────────────────
// Start server
// ─────────────────────────────────────────────────────────────
const javaOk = compileJava();
if (!javaOk) {
  console.warn('[Server] Java compilation failed — /api/* endpoints will return errors.');
  console.warn('[Server] UI will still load. Make sure JDK is installed and on PATH.');
}

app.listen(PORT, () => {
  console.log(`\n🚗 Parking System running at http://localhost:${PORT}`);
  console.log(`   Java: ${JAVA}`);
  console.log(`   Javac: ${JAVAC}`);
});
