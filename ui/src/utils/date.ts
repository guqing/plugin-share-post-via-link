import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'
import timezone from 'dayjs/plugin/timezone'
import utc from 'dayjs/plugin/utc'
dayjs.extend(timezone)
dayjs.extend(utc)
dayjs.extend(relativeTime)

export function formatDatetime(date: string | Date | undefined | null, tz?: string): string {
  if (!date) {
    return ''
  }
  return dayjs(date).tz(tz).format('YYYY-MM-DD HH:mm')
}

export function toISOString(date: string | Date | undefined | null): string {
  if (!date) {
    return ''
  }
  return dayjs(date).utc(false).toISOString()
}

export function toDatetimeLocal(date: string | Date | undefined | null, tz?: string): string {
  if (!date) {
    return ''
  }
  // see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local#the_y10k_problem_often_client-side
  return dayjs(date).tz(tz).format('YYYY-MM-DDTHH:mm')
}

/**
 * Get relative time to end date
 *
 * @param date end date
 * @returns relative time to end date
 *
 * @example
 *
 * // now is 2020-12-01
 * RelativeTimeTo("2021-01-01") // in 1 month
 */
export function relativeTimeTo(date: string | Date | undefined | null) {
  dayjs.locale('zh-cn')

  if (!date) {
    return
  }

  return dayjs().to(dayjs(date))
}
